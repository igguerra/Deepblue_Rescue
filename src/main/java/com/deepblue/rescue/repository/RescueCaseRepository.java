package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {

    // Paso 32 - Consulta A: buscar un caso por caseCode
    Optional<RescueCase> findByCaseCode(String caseCode);

    // Paso 32 - Consulta B: buscar todos los casos según status, ordenados por
    // rescueDate ASC
    List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);

    // Paso 32 - Consulta C / Paso 33: buscar casos de un centro determinado navegando
    List<RescueCase> findByRescueCenterCode(String centerCode);

    // Paso 42: casos posteriores a determinada fecha, ordenados del más reciente al
    // más antiguo
    List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDate date);
}
