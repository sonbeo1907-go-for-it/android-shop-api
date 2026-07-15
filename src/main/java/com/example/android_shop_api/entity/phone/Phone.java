package com.example.android_shop_api.entity.phone;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "phones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_phones_slug",
                        columnNames = "slug"
                )
        },
        indexes = {
                @Index(
                        name = "idx_phones_active",
                        columnList = "active"
                ),
                @Index(
                        name = "idx_phones_brand",
                        columnList = "brand"
                ),
                @Index(
                        name = "idx_phones_featured",
                        columnList = "featured"
                ),
                @Index(
                        name = "idx_phones_sold_count",
                        columnList = "sold_count"
                ),
                @Index(
                        name = "idx_phones_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 160
    )
    private String name;

    @Column(
            nullable = false,
            length = 180
    )
    private String slug;

    @Column(length = 120)
    private String model;

    @Column(
            nullable = false,
            length = 100
    )
    private String brand;

    @Column(
            name = "base_price",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(
            name = "original_price",
            precision = 15,
            scale = 0
    )
    private BigDecimal originalPrice;

    @Column(
            name = "short_description",
            length = 500
    )
    private String shortDescription;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "thumbnail_url",
            nullable = false,
            length = 1000
    )
    private String thumbnailUrl;

    /*
     * Lưu dưới dạng JSONB trong PostgreSQL.
     *
     * Ví dụ:
     * [
     *   "/images/phones/s25-1.webp",
     *   "/images/phones/s25-2.webp"
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "images",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private List<String> images = new ArrayList<>();

    /*
     * Lưu thông số kỹ thuật dạng JSONB.
     *
     * Ví dụ:
     * {
     *   "Màn hình": "6.8 inch AMOLED",
     *   "Chip": "Snapdragon 8 Elite",
     *   "Pin": "5000 mAh"
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "specifications",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, String> specifications = new LinkedHashMap<>();

    @Column(
            name = "stock_quantity",
            nullable = false
    )
    private Integer stockQuantity = 0;

    @Column(
            name = "sold_count",
            nullable = false
    )
    private Long soldCount = 0L;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(nullable = false)
    private boolean active = true;

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
            mappedBy = "phone",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("type ASC, displayOrder ASC, id ASC")
    private List<PhoneOption> options = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }

        if (stockQuantity == null) {
            stockQuantity = 0;
        }

        if (soldCount == null) {
            soldCount = 0L;
        }

        if (images == null) {
            images = new ArrayList<>();
        }

        if (specifications == null) {
            specifications = new LinkedHashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addOption(PhoneOption option) {
        if (option == null) {
            return;
        }

        options.add(option);
        option.setPhone(this);
    }

    public void removeOption(PhoneOption option) {
        if (option == null) {
            return;
        }

        options.remove(option);
        option.setPhone(null);
    }
}