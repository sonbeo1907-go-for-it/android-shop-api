package com.example.android_shop_api.entity.phone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "phone_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_phone_options_phone_type_value",
                        columnNames = {
                                "phone_id",
                                "option_type",
                                "option_value"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_phone_options_phone_id",
                        columnList = "phone_id"
                ),
                @Index(
                        name = "idx_phone_options_type",
                        columnList = "option_type"
                ),
                @Index(
                        name = "idx_phone_options_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PhoneOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "phone_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_phone_options_phone"
            )
    )
    private Phone phone;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "option_type",
            nullable = false,
            length = 20
    )
    private PhoneOptionType type;

    @Column(
            name = "option_value",
            nullable = false,
            length = 100
    )
    private String value;

    @Column(
            name = "extra_price",
            nullable = false,
            precision = 15,
            scale = 0
    )
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Column(
            name = "image_url",
            length = 1000
    )
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder = 0;

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

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (extraPrice == null) {
            extraPrice = BigDecimal.ZERO;
        }

        if (displayOrder == null) {
            displayOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}