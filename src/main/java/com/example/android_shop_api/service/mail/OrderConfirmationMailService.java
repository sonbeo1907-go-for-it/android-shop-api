package com.example.android_shop_api.service.mail;


import com.example.android_shop_api.entity.order.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConfirmationMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendOrderConfirmation(Order order) {
        if (!StringUtils.hasText(order.getEmail())) {
            return;
        }

        String lookupUrl =
                frontendUrl
                        + "/order-lookup?orderCode="
                        + order.getOrderCode();

        String totalAmount =
                NumberFormat
                        .getCurrencyInstance(
                                Locale.forLanguageTag("vi-VN")
                        )
                        .format(order.getTotalAmount());

        String emailBody = """
                Xin chào %s,

                Cửa hàng đã nhận được đơn hàng của bạn.

                Mã đơn hàng: %s
                Trạng thái đơn: %s
                Phương thức thanh toán: %s
                Tổng thanh toán: %s

                Địa chỉ nhận hàng:
                %s

                Tra cứu đơn hàng:
                %s

                Hãy lưu mã đơn và không chia sẻ thông tin đơn hàng cho người không liên quan.

                Cảm ơn bạn đã mua hàng.
                """.formatted(
                order.getReceiverName(),
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                totalAmount,
                order.getAddress(),
                lookupUrl
        );

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(order.getEmail());
        message.setSubject(
                "Xác nhận đơn hàng "
                        + order.getOrderCode()
        );
        message.setText(emailBody);

        try {
            mailSender.send(message);

            log.info(
                    "Order confirmation email sent. orderCode={}, email={}",
                    order.getOrderCode(),
                    order.getEmail()
            );
        } catch (MailException exception) {
            log.error(
                    "Could not send order confirmation email. orderCode={}, email={}",
                    order.getOrderCode(),
                    order.getEmail(),
                    exception
            );
        }
    }
}