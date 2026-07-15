package com.example.android_shop_api.service.phone.impl;

import com.example.android_shop_api.dto.common.PageResponse;
import com.example.android_shop_api.dto.phone.request.PhoneFilterRequest;
import com.example.android_shop_api.dto.phone.response.PhoneCardResponse;
import com.example.android_shop_api.dto.phone.response.PhoneDetailResponse;
import com.example.android_shop_api.dto.phone.response.PhoneOptionGroupResponse;
import com.example.android_shop_api.dto.phone.response.PhoneOptionResponse;
import com.example.android_shop_api.entity.phone.Phone;
import com.example.android_shop_api.entity.phone.PhoneOption;
import com.example.android_shop_api.entity.phone.PhoneOptionType;
import com.example.android_shop_api.exception.BusinessException;
import com.example.android_shop_api.exception.ResourceNotFoundException;
import com.example.android_shop_api.repository.phone.PhoneRepository;
import com.example.android_shop_api.service.phone.PhoneService;
import com.example.android_shop_api.specification.phone.PhoneSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PhoneServiceImpl implements PhoneService {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 50;

    private static final int MIN_LIST_LIMIT = 1;
    private static final int MAX_LIST_LIMIT = 20;

    /*
     * Key là giá trị Frontend gửi.
     * Value là field thật trong Phone entity.
     */
    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "price", "basePrice",
            "soldcount", "soldCount",
            "createdat", "createdAt",
            "name", "name"
    );

    private final PhoneRepository phoneRepository;

    public PhoneServiceImpl(
            PhoneRepository phoneRepository
    ) {
        this.phoneRepository = phoneRepository;
    }

    @Override
    public PageResponse<PhoneCardResponse> getPhones(
            PhoneFilterRequest request
    ) {
        validateFilterRequest(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                buildSort(
                        request.getSortBy(),
                        request.getSortDirection()
                )
        );

        Page<PhoneCardResponse> result =
                phoneRepository.findAll(
                                PhoneSpecification.withFilters(request),
                                pageable
                        )
                        .map(this::toCardResponse);

        return PageResponse.from(result);
    }

    @Override
    public PhoneDetailResponse getPhoneBySlug(
            String slug
    ) {
        if (!StringUtils.hasText(slug)) {
            throw new BusinessException(
                    "PHONE_INVALID_SLUG",
                    "Slug điện thoại không hợp lệ",
                    HttpStatus.BAD_REQUEST
            );
        }

        String normalizedSlug = slug.trim();

        Phone phone = phoneRepository
                .findBySlugIgnoreCaseAndActiveTrue(normalizedSlug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "PHONE_NOT_FOUND",
                                "Không tìm thấy điện thoại"
                        )
                );

        return toDetailResponse(phone);
    }

    @Override
    public List<PhoneCardResponse> getFeaturedPhones(
            int limit
    ) {
        validateLimit(limit);

        Pageable pageable = PageRequest.of(0, limit);

        return phoneRepository
                .findByActiveTrueAndFeaturedTrueOrderByCreatedAtDescIdDesc(
                        pageable
                )
                .stream()
                .map(this::toCardResponse)
                .toList();
    }

    @Override
    public List<PhoneCardResponse> getBestSellerPhones(
            int limit
    ) {
        validateLimit(limit);

        Pageable pageable = PageRequest.of(0, limit);

        return phoneRepository
                .findByActiveTrueOrderBySoldCountDescCreatedAtDescIdDesc(
                        pageable
                )
                .stream()
                .map(this::toCardResponse)
                .toList();
    }

    private void validateFilterRequest(
            PhoneFilterRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "PHONE_INVALID_FILTER",
                    "Bộ lọc điện thoại không hợp lệ",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getPage() < 0) {
            throw new BusinessException(
                    "PHONE_INVALID_PAGE",
                    "Số trang không được nhỏ hơn 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getSize() < MIN_PAGE_SIZE
                || request.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    "PHONE_INVALID_PAGE_SIZE",
                    "Kích thước trang phải nằm trong khoảng 1 đến 50",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getMinPrice() != null
                && request.getMaxPrice() != null
                && request.getMinPrice()
                .compareTo(request.getMaxPrice()) > 0) {
            throw new BusinessException(
                    "PHONE_INVALID_PRICE_RANGE",
                    "Giá tối thiểu không được lớn hơn giá tối đa",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateLimit(int limit) {
        if (limit < MIN_LIST_LIMIT
                || limit > MAX_LIST_LIMIT) {
            throw new BusinessException(
                    "PHONE_INVALID_LIMIT",
                    "Limit phải nằm trong khoảng 1 đến 20",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private Sort buildSort(
            String sortBy,
            String sortDirection
    ) {
        String normalizedSortBy =
                StringUtils.hasText(sortBy)
                        ? sortBy.trim().toLowerCase(Locale.ROOT)
                        : "createdat";

        String property = SORT_PROPERTIES.get(normalizedSortBy);

        if (property == null) {
            throw new BusinessException(
                    "PHONE_INVALID_SORT_FIELD",
                    "Chỉ hỗ trợ sắp xếp theo: "
                            + "price, soldCount, createdAt hoặc name",
                    HttpStatus.BAD_REQUEST
            );
        }

        Sort.Direction direction =
                parseSortDirection(sortDirection);

        Sort.Order primaryOrder =
                new Sort.Order(direction, property);

        /*
         * Sắp xếp tên không phân biệt chữ hoa và chữ thường.
         */
        if ("name".equals(property)) {
            primaryOrder = primaryOrder.ignoreCase();
        }

        /*
         * id là tie-breaker để phân trang ổn định.
         */
        Sort.Order idOrder =
                new Sort.Order(direction, "id");

        return Sort.by(primaryOrder, idOrder);
    }

    private Sort.Direction parseSortDirection(
            String sortDirection
    ) {
        if (!StringUtils.hasText(sortDirection)) {
            return Sort.Direction.DESC;
        }

        return switch (
                sortDirection.trim().toLowerCase(Locale.ROOT)
                ) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;

            default -> throw new BusinessException(
                    "PHONE_INVALID_SORT_DIRECTION",
                    "Hướng sắp xếp chỉ có thể là asc hoặc desc",
                    HttpStatus.BAD_REQUEST
            );
        };
    }

    private PhoneCardResponse toCardResponse(
            Phone phone
    ) {
        return new PhoneCardResponse(
                phone.getId(),
                phone.getName(),
                phone.getSlug(),
                phone.getModel(),
                phone.getBrand(),
                phone.getBasePrice(),
                phone.getOriginalPrice(),
                phone.getThumbnailUrl(),
                phone.getStockQuantity(),
                phone.getSoldCount(),
                phone.isFeatured()
        );
    }

    private PhoneDetailResponse toDetailResponse(
            Phone phone
    ) {
        return new PhoneDetailResponse(
                phone.getId(),
                phone.getName(),
                phone.getSlug(),
                phone.getModel(),
                phone.getBrand(),
                phone.getBasePrice(),
                phone.getOriginalPrice(),
                phone.getShortDescription(),
                phone.getDescription(),
                phone.getThumbnailUrl(),
                phone.getImages(),
                phone.getSpecifications(),
                phone.getStockQuantity(),
                phone.getSoldCount(),
                phone.isFeatured(),
                buildOptionGroups(phone.getOptions()),
                phone.getCreatedAt(),
                phone.getUpdatedAt()
        );
    }

    private List<PhoneOptionGroupResponse> buildOptionGroups(
            List<PhoneOption> options
    ) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        Map<PhoneOptionType, List<PhoneOptionResponse>> groupedOptions =
                new EnumMap<>(PhoneOptionType.class);

        for (PhoneOption option : options) {
            if (!option.isActive()) {
                continue;
            }

            groupedOptions
                    .computeIfAbsent(
                            option.getType(),
                            ignored -> new ArrayList<>()
                    )
                    .add(toOptionResponse(option));
        }

        Comparator<PhoneOptionResponse> optionComparator =
                Comparator
                        .comparingInt(
                                PhoneOptionResponse::displayOrder
                        )
                        .thenComparing(
                                PhoneOptionResponse::id
                        );

        groupedOptions.values().forEach(
                values -> values.sort(optionComparator)
        );

        /*
         * Thứ tự nhóm luôn theo enum:
         * COLOR → RAM → STORAGE
         */
        return Arrays.stream(PhoneOptionType.values())
                .filter(groupedOptions::containsKey)
                .map(type ->
                        new PhoneOptionGroupResponse(
                                type,
                                groupedOptions.get(type)
                        )
                )
                .toList();
    }

    private PhoneOptionResponse toOptionResponse(
            PhoneOption option
    ) {
        int displayOrder =
                option.getDisplayOrder() == null
                        ? 0
                        : option.getDisplayOrder();

        return new PhoneOptionResponse(
                option.getId(),
                option.getType(),
                option.getValue(),
                option.getExtraPrice(),
                option.getImageUrl(),
                displayOrder
        );
    }
}