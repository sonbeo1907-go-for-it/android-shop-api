package com.example.android_shop_api.entity.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_order_code",
                        columnNames = "order_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_orders_lookup",
                        columnList = "order_code, phone_number"
                ),
                @Index(
                        name = "idx_orders_phone_number",
                        columnList = "phone_number"
                ),
                @Index(
                        name = "idx_orders_status",
                        columnList = "order_status"
                ),
                @Index(
                        name = "idx_orders_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "order_code",
            nullable = false,
            length = 40
    )
    private String orderCode;

    @Column(
            name = "receiver_name",
            nullable = false,
            length = 120
    )
    private String receiverName;

    /*
     * Số điện thoại phải được chuẩn hóa trước khi lưu.
     * Ví dụ:
     * +84901234567 → 0901234567
     */
    @Column(
            name = "phone_number",
            nullable = false,
            length = 20
    )
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(
            nullable = false,
            length = 500
    )
    private String address;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            name = "shipping_fee",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 30
    )
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_status",
            nullable = false,
            length = 30
    )
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }

        if (items == null) {
            items = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            return;
        }

        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        if (item == null) {
            return;
        }

        items.remove(item);
        item.setOrder(null);
    }
}