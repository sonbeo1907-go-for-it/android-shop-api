package com.example.android_shop_api.repository.phone;

import com.example.android_shop_api.entity.phone.Phone;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PhoneRepository
        extends JpaRepository<Phone, Long>,
        JpaSpecificationExecutor<Phone> {

    Optional<Phone> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "options")
    Optional<Phone> findBySlugIgnoreCaseAndActiveTrue(
            String slug
    );

    boolean existsBySlugIgnoreCase(String slug);

    List<Phone>
    findByActiveTrueAndFeaturedTrueOrderByCreatedAtDescIdDesc(
            Pageable pageable
    );

    List<Phone>
    findByActiveTrueOrderBySoldCountDescCreatedAtDescIdDesc(
            Pageable pageable
    );

    /*
     * Khóa các sản phẩm trong transaction tạo đơn.
     *
     * Không lọc active tại đây để service phân biệt được:
     * - PHONE_NOT_FOUND
     * - PHONE_INACTIVE
     *
     * ORDER BY id giúp các transaction luôn khóa Phone
     * theo cùng thứ tự, giảm nguy cơ deadlock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT phone
            FROM Phone phone
            WHERE phone.id IN :ids
            ORDER BY phone.id ASC
            """)
    List<Phone> findAllByIdInForUpdate(
            @Param("ids") Collection<Long> ids
    );
}