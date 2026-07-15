package com.example.android_shop_api.repository.phone;

import com.example.android_shop_api.entity.phone.PhoneOption;
import com.example.android_shop_api.entity.phone.PhoneOptionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PhoneOptionRepository
        extends JpaRepository<PhoneOption, Long> {

    List<PhoneOption>
    findByPhoneIdAndActiveTrueOrderByTypeAscDisplayOrderAscIdAsc(
            Long phoneId
    );

    @EntityGraph(attributePaths = "phone")
    List<PhoneOption> findByIdInAndActiveTrue(
            Collection<Long> ids
    );

    boolean existsByPhoneIdAndTypeAndValueIgnoreCase(
            Long phoneId,
            PhoneOptionType type,
            String value
    );
}