package com.example.android_shop_api.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(
                        name = "idx_order_items_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_order_items_phone_id",
                        columnList = "phone_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_order_items_order"
            )
    )
    private Order order;

    /*
     * Chỉ lưu ID tham chiếu của Phone.
     * Không tạo quan hệ ManyToOne để Order Item không phụ thuộc
     * vào trạng thái hoặc thay đổi của Phone hiện tại.
     */
    @Column(
            name = "phone_id",
            nullable = false
    )
    private Long phoneId;

    @Column(
            name = "phone_name_snapshot",
            nullable = false,
            length = 180
    )
    private String phoneNameSnapshot;

    @Column(
            name = "image_snapshot",
            nullable = false,
            length = 1000
    )
    private String imageSnapshot;

    @Column(
            name = "base_price_snapshot",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal basePriceSnapshot = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "selected_options",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private List<OrderItemOptionSnapshot> selectedOptions =
            new ArrayList<>();

    /*
     * unitPrice =
     * basePriceSnapshot + tổng extraPrice của selectedOptions
     */
    @Column(
            name = "unit_price",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer quantity;

    /*
     * totalPrice = unitPrice × quantity
     */
    @Column(
            name = "total_price",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (basePriceSnapshot == null) {
            basePriceSnapshot = BigDecimal.ZERO;
        }

        if (selectedOptions == null) {
            selectedOptions = new ArrayList<>();
        }

        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        if (totalPrice == null) {
            totalPrice = BigDecimal.ZERO;
        }
    }
}