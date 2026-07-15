package com.example.android_shop_api.service.order.impl;

import com.example.android_shop_api.dto.order.request.CreateOrderItemRequest;
import com.example.android_shop_api.dto.order.request.CreateOrderRequest;
import com.example.android_shop_api.dto.order.response.OrderItemOptionResponse;
import com.example.android_shop_api.dto.order.response.OrderItemResponse;
import com.example.android_shop_api.dto.order.response.OrderResponse;
import com.example.android_shop_api.entity.order.Order;
import com.example.android_shop_api.entity.order.OrderItem;
import com.example.android_shop_api.entity.order.OrderItemOptionSnapshot;
import com.example.android_shop_api.entity.order.OrderStatus;
import com.example.android_shop_api.entity.order.PaymentMethod;
import com.example.android_shop_api.entity.order.PaymentStatus;
import com.example.android_shop_api.entity.phone.Phone;
import com.example.android_shop_api.entity.phone.PhoneOption;
import com.example.android_shop_api.entity.phone.PhoneOptionType;
import com.example.android_shop_api.event.OrderCreatedEvent;
import com.example.android_shop_api.exception.BusinessException;
import com.example.android_shop_api.exception.ResourceNotFoundException;
import com.example.android_shop_api.repository.order.OrderRepository;
import com.example.android_shop_api.repository.phone.PhoneOptionRepository;
import com.example.android_shop_api.repository.phone.PhoneRepository;
import com.example.android_shop_api.service.order.OrderService;
import com.example.android_shop_api.util.OrderCodeGenerator;
import com.example.android_shop_api.util.PhoneNumberUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal SHIPPING_FEE =
            BigDecimal.ZERO;

    private static final int MAX_QUANTITY_PER_ITEM = 10;

    private static final int MAX_ORDER_ITEMS = 20;

    private static final int MAX_ORDER_CODE_ATTEMPTS = 10;

    private final ApplicationEventPublisher eventPublisher;

    private static final Set<PhoneOptionType>
            REQUIRED_OPTION_TYPES =
            Set.copyOf(
                    EnumSet.allOf(PhoneOptionType.class)
            );

    private final OrderRepository orderRepository;
    private final PhoneRepository phoneRepository;
    private final PhoneOptionRepository phoneOptionRepository;
    private final OrderCodeGenerator orderCodeGenerator;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            PhoneRepository phoneRepository,
            PhoneOptionRepository phoneOptionRepository,
            OrderCodeGenerator orderCodeGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.phoneRepository = phoneRepository;
        this.phoneOptionRepository = phoneOptionRepository;
        this.orderCodeGenerator = orderCodeGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request
    ) {
        validateCreateOrderRequest(request);

        String normalizedPhoneNumber =
                normalizeAndValidatePhoneNumber(
                        request.getPhoneNumber()
                );

        Map<Long, Integer> requestedQuantityByPhone =
                aggregateRequestedQuantities(
                        request.getItems()
                );

        List<Phone> lockedPhones =
                phoneRepository.findAllByIdInForUpdate(
                        requestedQuantityByPhone.keySet()
                );

        Map<Long, Phone> phoneById =
                lockedPhones.stream()
                        .collect(
                                Collectors.toMap(
                                        Phone::getId,
                                        Function.identity()
                                )
                        );

        validatePhones(
                requestedQuantityByPhone,
                phoneById
        );

        Order order = createOrderEntity(
                request,
                normalizedPhoneNumber
        );

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest
                : request.getItems()) {

            Phone phone = phoneById.get(
                    itemRequest.getPhoneId()
            );

            OrderItem orderItem = createOrderItem(
                    itemRequest,
                    phone
            );

            order.addItem(orderItem);

            subtotal = subtotal.add(
                    orderItem.getTotalPrice()
            );
        }

        order.setSubtotal(subtotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setTotalAmount(
                subtotal.add(SHIPPING_FEE)
        );

        decreaseStocks(
                requestedQuantityByPhone,
                phoneById
        );

        Order savedOrder =
                orderRepository.save(order);

        /*
         * Listener chỉ gửi email sau khi
         * transaction tạo đơn commit thành công.
         */
        eventPublisher.publishEvent(
                new OrderCreatedEvent(
                        savedOrder.getId()
                )
        );

        return toOrderResponse(
                savedOrder
        );
    }

    @Override
    public OrderResponse lookupOrder(
            String orderCode,
            String phoneNumber
    ) {
        String normalizedOrderCode =
                normalizeOrderCode(orderCode);

        String normalizedPhoneNumber =
                normalizeAndValidatePhoneNumber(
                        phoneNumber
                );

        /*
         * Luôn tìm bằng cả orderCode và phoneNumber.
         *
         * Mã đơn đúng nhưng số điện thoại sai
         * cũng trả ORDER_NOT_FOUND.
         */
        Order order = orderRepository
                .findByOrderCodeIgnoreCaseAndPhoneNumber(
                        normalizedOrderCode,
                        normalizedPhoneNumber
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "ORDER_NOT_FOUND",
                                "Không tìm thấy đơn hàng"
                        )
                );

        return toOrderResponse(order);
    }

    private void validateCreateOrderRequest(
            CreateOrderRequest request
    ) {
        if (request == null) {
            throw badRequest(
                    "INVALID_REQUEST",
                    "Dữ liệu đơn hàng không hợp lệ"
            );
        }

        if (!StringUtils.hasText(
                request.getReceiverName()
        )) {
            throw badRequest(
                    "INVALID_RECEIVER_NAME",
                    "Tên người nhận không được để trống"
            );
        }

        if (!StringUtils.hasText(
                request.getAddress()
        )) {
            throw badRequest(
                    "INVALID_ADDRESS",
                    "Địa chỉ nhận hàng không được để trống"
            );
        }

        if (request.getPaymentMethod() == null) {
            throw badRequest(
                    "INVALID_PAYMENT_METHOD",
                    "Phương thức thanh toán không hợp lệ"
            );
        }

        List<CreateOrderItemRequest> items =
                request.getItems();

        if (items == null || items.isEmpty()) {
            throw badRequest(
                    "EMPTY_ORDER",
                    "Đơn hàng phải có ít nhất một sản phẩm"
            );
        }

        if (items.size() > MAX_ORDER_ITEMS) {
            throw badRequest(
                    "TOO_MANY_ORDER_ITEMS",
                    "Đơn hàng không được vượt quá 20 dòng sản phẩm"
            );
        }

        for (CreateOrderItemRequest item : items) {
            validateOrderItemRequest(item);
        }
    }

    private void validateOrderItemRequest(
            CreateOrderItemRequest item
    ) {
        if (item == null) {
            throw badRequest(
                    "INVALID_ORDER_ITEM",
                    "Dòng sản phẩm không hợp lệ"
            );
        }

        if (item.getPhoneId() == null
                || item.getPhoneId() <= 0) {
            throw badRequest(
                    "INVALID_PHONE_ID",
                    "ID điện thoại không hợp lệ"
            );
        }

        if (item.getQuantity() == null
                || item.getQuantity() < 1
                || item.getQuantity()
                > MAX_QUANTITY_PER_ITEM) {
            throw badRequest(
                    "INVALID_QUANTITY",
                    "Số lượng sản phẩm phải nằm trong khoảng 1 đến 10"
            );
        }

        if (item.getOptionIds() == null
                || item.getOptionIds().isEmpty()) {
            throw badRequest(
                    "MISSING_REQUIRED_OPTION",
                    "Phải chọn đầy đủ option cho sản phẩm"
            );
        }

        if (item.getOptionIds().stream()
                .anyMatch(Objects::isNull)) {
            throw badRequest(
                    "INVALID_OPTION_ID",
                    "ID option không hợp lệ"
            );
        }
    }

    private Map<Long, Integer>
    aggregateRequestedQuantities(
            List<CreateOrderItemRequest> items
    ) {
        /*
         * TreeMap giữ Phone ID tăng dần.
         */
        Map<Long, Integer> quantities =
                new TreeMap<>();

        for (CreateOrderItemRequest item : items) {
            quantities.merge(
                    item.getPhoneId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        return quantities;
    }

    private void validatePhones(
            Map<Long, Integer> requestedQuantityByPhone,
            Map<Long, Phone> phoneById
    ) {
        for (Map.Entry<Long, Integer> entry
                : requestedQuantityByPhone.entrySet()) {

            Long phoneId = entry.getKey();
            int requestedQuantity = entry.getValue();

            Phone phone = phoneById.get(phoneId);

            if (phone == null) {
                throw new ResourceNotFoundException(
                        "PHONE_NOT_FOUND",
                        "Không tìm thấy điện thoại"
                );
            }

            if (!phone.isActive()) {
                throw new BusinessException(
                        "PHONE_INACTIVE",
                        "Điện thoại hiện không còn được bán",
                        HttpStatus.CONFLICT
                );
            }

            int availableStock =
                    phone.getStockQuantity() == null
                            ? 0
                            : phone.getStockQuantity();

            if (availableStock < requestedQuantity) {
                throw new BusinessException(
                        "OUT_OF_STOCK",
                        "Sản phẩm "
                                + phone.getName()
                                + " không đủ số lượng tồn kho",
                        HttpStatus.CONFLICT
                );
            }
        }
    }

    private Order createOrderEntity(
            CreateOrderRequest request,
            String normalizedPhoneNumber
    ) {
        Order order = new Order();

        order.setOrderCode(
                generateUniqueOrderCode()
        );

        order.setReceiverName(
                request.getReceiverName().trim()
        );

        order.setPhoneNumber(
                normalizedPhoneNumber
        );

        order.setEmail(
                normalizeNullableText(
                        request.getEmail()
                )
        );

        order.setAddress(
                request.getAddress().trim()
        );

        order.setNote(
                normalizeNullableText(
                        request.getNote()
                )
        );

        order.setPaymentMethod(
                request.getPaymentMethod()
        );

        order.setPaymentStatus(
                determineInitialPaymentStatus(
                        request.getPaymentMethod()
                )
        );

        order.setOrderStatus(
                OrderStatus.PENDING
        );

        return order;
    }

    private OrderItem createOrderItem(
            CreateOrderItemRequest request,
            Phone phone
    ) {
        List<PhoneOption> selectedOptions =
                loadAndValidateOptions(
                        request.getOptionIds(),
                        phone
                );

        List<OrderItemOptionSnapshot> snapshots =
                new ArrayList<>(
                        selectedOptions.size()
                );

        BigDecimal optionExtraPrice =
                BigDecimal.ZERO;

        for (PhoneOption option : selectedOptions) {
            BigDecimal extraPrice =
                    option.getExtraPrice() == null
                            ? BigDecimal.ZERO
                            : option.getExtraPrice();

            snapshots.add(
                    new OrderItemOptionSnapshot(
                            option.getId(),
                            option.getType(),
                            option.getValue(),
                            extraPrice
                    )
            );

            optionExtraPrice =
                    optionExtraPrice.add(extraPrice);
        }

        BigDecimal basePrice =
                phone.getBasePrice() == null
                        ? BigDecimal.ZERO
                        : phone.getBasePrice();

        BigDecimal unitPrice =
                basePrice.add(optionExtraPrice);

        BigDecimal totalPrice =
                unitPrice.multiply(
                        BigDecimal.valueOf(
                                request.getQuantity()
                        )
                );

        OrderItem orderItem = new OrderItem();

        orderItem.setPhoneId(
                phone.getId()
        );

        orderItem.setPhoneNameSnapshot(
                phone.getName()
        );

        orderItem.setImageSnapshot(
                phone.getThumbnailUrl()
        );

        orderItem.setBasePriceSnapshot(
                basePrice
        );

        orderItem.setSelectedOptions(
                snapshots
        );

        orderItem.setUnitPrice(
                unitPrice
        );

        orderItem.setQuantity(
                request.getQuantity()
        );

        orderItem.setTotalPrice(
                totalPrice
        );

        return orderItem;
    }

    private List<PhoneOption> loadAndValidateOptions(
            Collection<Long> rawOptionIds,
            Phone phone
    ) {
        /*
         * Loại bỏ trùng nhưng vẫn phát hiện nếu request
         * đã gửi cùng một option ID nhiều lần.
         */
        LinkedHashSet<Long> uniqueOptionIds =
                new LinkedHashSet<>(rawOptionIds);

        if (uniqueOptionIds.size()
                != rawOptionIds.size()) {
            throw badRequest(
                    "DUPLICATE_OPTION_ID",
                    "Không được chọn trùng cùng một option"
            );
        }

        List<PhoneOption> options =
                phoneOptionRepository
                        .findByIdInAndActiveTrue(
                                uniqueOptionIds
                        );

        if (options.size()
                != uniqueOptionIds.size()) {
            throw new ResourceNotFoundException(
                    "OPTION_NOT_FOUND",
                    "Một hoặc nhiều option không tồn tại hoặc đã bị vô hiệu hóa"
            );
        }

        /*
         * Sắp xếp snapshot ổn định:
         * COLOR → RAM → STORAGE.
         */
        options.sort(
                Comparator
                        .comparing(
                                PhoneOption::getType
                        )
                        .thenComparingInt(
                                option ->
                                        option.getDisplayOrder()
                                                == null
                                                ? 0
                                                : option.getDisplayOrder()
                        )
                        .thenComparing(
                                PhoneOption::getId
                        )
        );

        Map<PhoneOptionType, PhoneOption> optionByType =
                new EnumMap<>(
                        PhoneOptionType.class
                );

        for (PhoneOption option : options) {
            Long optionPhoneId =
                    option.getPhone().getId();

            if (!phone.getId().equals(
                    optionPhoneId
            )) {
                throw badRequest(
                        "INVALID_PHONE_OPTION",
                        "Option không thuộc điện thoại đã chọn"
                );
            }

            PhoneOption previous =
                    optionByType.putIfAbsent(
                            option.getType(),
                            option
                    );

            if (previous != null) {
                throw badRequest(
                        "DUPLICATE_OPTION_TYPE",
                        "Không được chọn nhiều option cùng loại "
                                + option.getType()
                );
            }
        }

        EnumSet<PhoneOptionType> missingTypes =
                EnumSet.allOf(
                        PhoneOptionType.class
                );

        missingTypes.removeAll(
                optionByType.keySet()
        );

        if (!missingTypes.isEmpty()) {
            throw badRequest(
                    "MISSING_REQUIRED_OPTION",
                    "Thiếu option bắt buộc: "
                            + missingTypes
            );
        }

        /*
         * Phòng trường hợp enum sau này có thêm loại không bắt buộc.
         * Với hiện tại, tập bắt buộc gồm COLOR, RAM, STORAGE.
         */
        if (!optionByType.keySet()
                .containsAll(REQUIRED_OPTION_TYPES)) {
            throw badRequest(
                    "MISSING_REQUIRED_OPTION",
                    "Phải chọn đủ COLOR, RAM và STORAGE"
            );
        }

        return options;
    }

    private void decreaseStocks(
            Map<Long, Integer> requestedQuantityByPhone,
            Map<Long, Phone> phoneById
    ) {
        for (Map.Entry<Long, Integer> entry
                : requestedQuantityByPhone.entrySet()) {

            Phone phone = phoneById.get(
                    entry.getKey()
            );

            int remainingStock =
                    phone.getStockQuantity()
                            - entry.getValue();

            phone.setStockQuantity(
                    remainingStock
            );
        }
    }

    private PaymentStatus
    determineInitialPaymentStatus(
            PaymentMethod paymentMethod
    ) {
        return switch (paymentMethod) {
            case COD -> PaymentStatus.UNPAID;

            case QR_TRANSFER ->
                    PaymentStatus.PENDING_CONFIRMATION;
        };
    }

    private String generateUniqueOrderCode() {
        for (int attempt = 0;
             attempt < MAX_ORDER_CODE_ATTEMPTS;
             attempt++) {

            String generatedCode =
                    orderCodeGenerator.generate();

            boolean alreadyExists =
                    orderRepository
                            .existsByOrderCodeIgnoreCase(
                                    generatedCode
                            );

            if (!alreadyExists) {
                return generatedCode;
            }
        }

        throw new BusinessException(
                "ORDER_CODE_GENERATION_FAILED",
                "Không thể tạo mã đơn hàng",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private String normalizeAndValidatePhoneNumber(
            String rawPhoneNumber
    ) {
        String normalized =
                PhoneNumberUtils.normalize(
                        rawPhoneNumber
                );

        if (!PhoneNumberUtils
                .isValidVietnameseMobile(normalized)) {
            throw badRequest(
                    "INVALID_PHONE_NUMBER",
                    "Số điện thoại không hợp lệ"
            );
        }

        return normalized;
    }

    private String normalizeOrderCode(
            String orderCode
    ) {
        if (!StringUtils.hasText(orderCode)) {
            throw badRequest(
                    "INVALID_ORDER_CODE",
                    "Mã đơn hàng không được để trống"
            );
        }

        return orderCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeNullableText(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private OrderResponse toOrderResponse(
            Order order
    ) {
        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(this::toOrderItemResponse)
                        .toList();

        return new OrderResponse(
                order.getOrderCode(),
                order.getReceiverName(),
                order.getPhoneNumber(),
                order.getEmail(),
                order.getAddress(),
                order.getNote(),
                itemResponses,
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getOrderStatus(),
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toOrderItemResponse(
            OrderItem item
    ) {
        List<OrderItemOptionResponse> optionResponses =
                item.getSelectedOptions()
                        .stream()
                        .map(this::toOptionResponse)
                        .toList();

        return new OrderItemResponse(
                item.getPhoneId(),
                item.getPhoneNameSnapshot(),
                item.getImageSnapshot(),
                item.getBasePriceSnapshot(),
                optionResponses,
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice()
        );
    }

    private OrderItemOptionResponse toOptionResponse(
            OrderItemOptionSnapshot snapshot
    ) {
        return new OrderItemOptionResponse(
                snapshot.getOptionId(),
                snapshot.getType(),
                snapshot.getValue(),
                snapshot.getExtraPrice()
        );
    }

    private BusinessException badRequest(
            String code,
            String message
    ) {
        return new BusinessException(
                code,
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}