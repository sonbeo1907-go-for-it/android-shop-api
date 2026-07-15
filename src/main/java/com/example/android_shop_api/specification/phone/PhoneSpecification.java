package com.example.android_shop_api.specification.phone;

import com.example.android_shop_api.dto.phone.request.PhoneFilterRequest;
import com.example.android_shop_api.entity.phone.Phone;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PhoneSpecification {

    private PhoneSpecification() {
    }

    public static Specification<Phone> withFilters(
            PhoneFilterRequest request
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            /*
             * Public API chỉ được trả điện thoại đang hoạt động.
             */
            predicates.add(
                    criteriaBuilder.isTrue(root.get("active"))
            );

            /*
             * Tìm kiếm không phân biệt hoa thường theo:
             * - name
             * - model
             * - brand
             */
            if (StringUtils.hasText(request.getKeyword())) {
                String normalizedKeyword = request
                        .getKeyword()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                String keywordPattern =
                        "%" + escapeLike(normalizedKeyword) + "%";

                Expression<String> name =
                        criteriaBuilder.lower(root.get("name"));

                Expression<String> model =
                        criteriaBuilder.lower(root.get("model"));

                Expression<String> brand =
                        criteriaBuilder.lower(root.get("brand"));

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        name,
                                        keywordPattern,
                                        '\\'
                                ),
                                criteriaBuilder.like(
                                        model,
                                        keywordPattern,
                                        '\\'
                                ),
                                criteriaBuilder.like(
                                        brand,
                                        keywordPattern,
                                        '\\'
                                )
                        )
                );
            }

            /*
             * Lọc hãng chính xác nhưng không phân biệt hoa thường.
             */
            if (StringUtils.hasText(request.getBrand())) {
                String normalizedBrand = request
                        .getBrand()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(
                                        root.get("brand")
                                ),
                                normalizedBrand
                        )
                );
            }

            if (request.getMinPrice() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("basePrice"),
                                request.getMinPrice()
                        )
                );
            }

            if (request.getMaxPrice() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("basePrice"),
                                request.getMaxPrice()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    /*
     * Ngăn ký tự % và _ trong keyword bị hiểu thành wildcard SQL.
     */
    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}