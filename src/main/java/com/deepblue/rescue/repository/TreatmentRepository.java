package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    // Paso 41: tratamientos de un animal, ordenados cronológicamente (Query Method)
    // Entrada: animal.id -> Camino: Treatment -> animal -> id
    List<Treatment> findByAnimalIdOrderByPerformedAtAsc(String animalId);

    // Paso 42: tratamientos realizados entre dos fechas (JPQL)
    @Query("""
        select t
        from Treatment t
        where t.performedAt between :start and :end
        order by t.performedAt asc
        """)
    List<Treatment> findBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Paso 43: tratamientos de animales pertenecientes a un centro determinado
    // (JPQL)
    // Camino: Treatment -> Animal -> RescueCase -> RescueCenter

    @Query("""
            select t
            from Treatment t
            where t.animal.rescueCase.rescueCenter.code = :centerCode
            """)
    List<Treatment> findByCenterCode(@Param("centerCode") String centerCode);

    // Paso 44: tratamientos realizados por especialistas con determinada
    // experiencia (JPQL, N:M)
    // Camino: Treatment -> Specialist -> Expertise
    @Query("""
            select distinct t
            from Treatment t
            join t.specialist s
            join s.expertiseAreas e
            where lower(e.name) = lower(:expertiseName)
            """)
    List<Treatment> findBySpecialistExpertise(@Param("expertiseName") String expertiseName);
}
