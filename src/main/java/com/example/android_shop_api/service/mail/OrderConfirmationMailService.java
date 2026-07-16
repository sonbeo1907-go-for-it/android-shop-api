package com.example.android_shop_api.service.mail;

import com.example.android_shop_api.entity.order.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.mail",
        name = "enabled",
        havingValue = "true"
)
public class OrderConfirmationMailService {

    private static final String LOGO_CONTENT_ID =
            "androidShopLogo";

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("classpath:mail/android-shop-logo.png")
    private Resource logoResource;

    public void sendOrderConfirmation(Order order) {
        if (!StringUtils.hasText(order.getEmail())) {
            return;
        }

        String normalizedFrontendUrl =
                frontendUrl.replaceAll("/+$", "");

        String encodedOrderCode =
                URLEncoder.encode(
                        order.getOrderCode(),
                        StandardCharsets.UTF_8
                );

        String lookupUrl =
                normalizedFrontendUrl
                        + "/order-lookup?orderCode="
                        + encodedOrderCode;

        String totalAmount =
                NumberFormat
                        .getCurrencyInstance(
                                Locale.forLanguageTag("vi-VN")
                        )
                        .format(order.getTotalAmount());

        String plainTextBody =
                createPlainTextBody(
                        order,
                        totalAmount,
                        lookupUrl
                );

        String htmlBody =
                createHtmlBody(
                        order,
                        totalAmount,
                        lookupUrl
                );

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(fromAddress);
            helper.setTo(order.getEmail());
            helper.setSubject(
                    "Xác nhận đơn hàng "
                            + order.getOrderCode()
            );

            // Plain text fallback + HTML version
            helper.setText(
                    plainTextBody,
                    htmlBody
            );

            helper.addInline(
                    LOGO_CONTENT_ID,
                    logoResource,
                    MediaType.IMAGE_PNG_VALUE
            );

            mailSender.send(message);

            log.info(
                    "Order confirmation email sent. orderCode={}, email={}",
                    order.getOrderCode(),
                    order.getEmail()
            );
        } catch (MessagingException | MailException exception) {
            log.error(
                    "Could not send order confirmation email. orderCode={}, email={}",
                    order.getOrderCode(),
                    order.getEmail(),
                    exception
            );
        }
    }

    private String createPlainTextBody(
            Order order,
            String totalAmount,
            String lookupUrl
    ) {
        return """
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

                Hãy lưu mã đơn và không chia sẻ thông tin đơn hàng
                cho người không liên quan.

                Cảm ơn bạn đã mua hàng tại Android Shop.
                """.formatted(
                order.getReceiverName(),
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                totalAmount,
                order.getAddress(),
                lookupUrl
        );
    }

    private String createHtmlBody(
            Order order,
            String totalAmount,
            String lookupUrl
    ) {
        String receiverName =
                escape(order.getReceiverName());

        String orderCode =
                escape(order.getOrderCode());

        String orderStatus =
                escape(String.valueOf(
                        order.getOrderStatus()
                ));

        String paymentMethod =
                escape(String.valueOf(
                        order.getPaymentMethod()
                ));

        String escapedTotalAmount =
                escape(totalAmount);

        String address =
                escape(order.getAddress());

        String escapedLookupUrl =
                escape(lookupUrl);

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Xác nhận đơn hàng</title>
                </head>

                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #f3f4f6;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #1f2937;
                ">
                    <table
                        role="presentation"
                        width="100%%"
                        cellspacing="0"
                        cellpadding="0"
                        style="background-color: #f3f4f6;"
                    >
                        <tr>
                            <td align="center"
                                style="padding: 32px 16px;">

                                <table
                                    role="presentation"
                                    width="100%%"
                                    cellspacing="0"
                                    cellpadding="0"
                                    style="
                                        max-width: 620px;
                                        background-color: #ffffff;
                                        border-radius: 14px;
                                        overflow: hidden;
                                        box-shadow:
                                            0 4px 18px
                                            rgba(0, 0, 0, 0.08);
                                    "
                                >
                                    <tr>
                                        <td style="
                                            padding: 24px 32px;
                                            background-color: #dc2626;
                                            text-align: center;
                                        ">
                                            <img
                                                src="cid:%s"
                                                alt="Android Shop"
                                                width="170"
                                                style="
                                                    display: inline-block;
                                                    max-width: 170px;
                                                    height: auto;
                                                    border: 0;
                                                "
                                            >
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding: 32px;">
                                            <h1 style="
                                                margin: 0 0 16px;
                                                font-size: 24px;
                                                line-height: 1.3;
                                                color: #111827;
                                            ">
                                                Đặt hàng thành công
                                            </h1>

                                            <p style="
                                                margin: 0 0 20px;
                                                font-size: 15px;
                                                line-height: 1.7;
                                            ">
                                                Xin chào
                                                <strong>%s</strong>,
                                                Android Shop đã nhận được
                                                đơn hàng của bạn.
                                            </p>

                                            <table
                                                role="presentation"
                                                width="100%%"
                                                cellspacing="0"
                                                cellpadding="0"
                                                style="
                                                    margin-bottom: 24px;
                                                    border-collapse: collapse;
                                                    background-color: #f9fafb;
                                                    border: 1px solid #e5e7eb;
                                                    border-radius: 10px;
                                                "
                                            >
                                                <tr>
                                                    <td style="%s">
                                                        Mã đơn hàng
                                                    </td>
                                                    <td style="%s">
                                                        <strong>%s</strong>
                                                    </td>
                                                </tr>

                                                <tr>
                                                    <td style="%s">
                                                        Trạng thái
                                                    </td>
                                                    <td style="%s">
                                                        %s
                                                    </td>
                                                </tr>

                                                <tr>
                                                    <td style="%s">
                                                        Thanh toán
                                                    </td>
                                                    <td style="%s">
                                                        %s
                                                    </td>
                                                </tr>

                                                <tr>
                                                    <td style="%s">
                                                        Tổng thanh toán
                                                    </td>
                                                    <td style="%s">
                                                        <strong
                                                            style="
                                                                color: #dc2626;
                                                                font-size: 17px;
                                                            "
                                                        >
                                                            %s
                                                        </strong>
                                                    </td>
                                                </tr>
                                            </table>

                                            <h2 style="
                                                margin: 0 0 8px;
                                                font-size: 16px;
                                                color: #111827;
                                            ">
                                                Địa chỉ nhận hàng
                                            </h2>

                                            <p style="
                                                margin: 0 0 24px;
                                                padding: 14px;
                                                background-color: #f9fafb;
                                                border-radius: 8px;
                                                font-size: 14px;
                                                line-height: 1.6;
                                            ">
                                                %s
                                            </p>

                                            <div style="
                                                margin: 28px 0;
                                                text-align: center;
                                            ">
                                                <a
                                                    href="%s"
                                                    style="
                                                        display: inline-block;
                                                        padding: 13px 24px;
                                                        border-radius: 8px;
                                                        background-color: #dc2626;
                                                        color: #ffffff;
                                                        font-size: 15px;
                                                        font-weight: bold;
                                                        text-decoration: none;
                                                    "
                                                >
                                                    Tra cứu đơn hàng
                                                </a>
                                            </div>

                                            <p style="
                                                margin: 0;
                                                font-size: 13px;
                                                line-height: 1.6;
                                                color: #6b7280;
                                            ">
                                                Hãy lưu mã đơn hàng và
                                                không chia sẻ thông tin
                                                đơn hàng cho người không
                                                liên quan.
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding: 20px 32px;
                                            background-color: #111827;
                                            text-align: center;
                                            color: #d1d5db;
                                            font-size: 12px;
                                            line-height: 1.6;
                                        ">
                                            Cảm ơn bạn đã mua hàng tại
                                            Android Shop.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                LOGO_CONTENT_ID,
                receiverName,

                labelCellStyle(),
                valueCellStyle(),
                orderCode,

                labelCellStyle(),
                valueCellStyle(),
                orderStatus,

                labelCellStyle(),
                valueCellStyle(),
                paymentMethod,

                labelCellStyle(),
                valueCellStyle(),
                escapedTotalAmount,

                address,
                escapedLookupUrl
        );
    }

    private String labelCellStyle() {
        return """
                padding: 12px 14px;
                border-bottom: 1px solid #e5e7eb;
                color: #6b7280;
                font-size: 14px;
                """.replace("\n", " ");
    }

    private String valueCellStyle() {
        return """
                padding: 12px 14px;
                border-bottom: 1px solid #e5e7eb;
                text-align: right;
                color: #111827;
                font-size: 14px;
                """.replace("\n", " ");
    }

    private String escape(Object value) {
        return HtmlUtils.htmlEscape(
                value == null
                        ? ""
                        : String.valueOf(value)
        );
    }
}