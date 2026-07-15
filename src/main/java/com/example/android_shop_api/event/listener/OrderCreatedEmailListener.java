package com.example.android_shop_api.event.listener;


import com.example.android_shop_api.entity.order.Order;
import com.example.android_shop_api.event.OrderCreatedEvent;
import com.example.android_shop_api.repository.order.OrderRepository;
import com.example.android_shop_api.service.mail.OrderConfirmationMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.mail",
        name = "enabled",
        havingValue = "true"
)
public class OrderCreatedEmailListener {

    private final OrderRepository orderRepository;

    private final OrderConfirmationMailService
            orderConfirmationMailService;

    @Async
    @Transactional(
            readOnly = true,
            propagation = Propagation.REQUIRES_NEW
    )
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(
            OrderCreatedEvent event
    ) {
        try {
            Order order = orderRepository
                    .findById(event.orderId())
                    .orElse(null);

            if (order == null) {
                log.warn(
                        "Order not found while preparing confirmation email. orderId={}",
                        event.orderId()
                );
                return;
            }

            if (order.getEmail() == null
                    || order.getEmail().isBlank()) {
                log.debug(
                        "Order has no email. Skip confirmation email. orderCode={}",
                        order.getOrderCode()
                );
                return;
            }

            orderConfirmationMailService
                    .sendOrderConfirmation(order);

        } catch (Exception exception) {
            log.error(
                    "Could not process order confirmation email. orderId={}",
                    event.orderId(),
                    exception
            );
        }
    }
}