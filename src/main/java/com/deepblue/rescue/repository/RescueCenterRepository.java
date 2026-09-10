package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RescueCenterRepository extends JpaRepository<RescueCenter, Long> {

    // Paso 31: buscar un centro por su código (Query Method, sin @Query)
    Optional<RescueCenter> findByCode(String code);
}
