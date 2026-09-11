package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // Paso 34 - Consulta A: buscar animal por animalCode
    Optional<Animal> findByAnimalCode(String animalCode);

    // Paso 34 - Consulta B: buscar animales cuyo commonName contenga determinado texto,
    // ignorando mayúsculas/minúsculas
    List<Animal> findByCommonNameContainingIgnoreCase(String text);

    // Paso 35: animales cuyo caso de rescate tenga determinado estado
    // Camino: Animal -> rescueCase -> status
    List<Animal> findByRescueCaseStatus(RescueStatus status);

    // Paso 36: animales pertenecientes a un centro determinado
    // Camino: Animal -> RescueCase -> RescueCenter -> code
    List<Animal> findByRescueCaseRescueCenterCode(String centerCode);

    // PARTE XIII (reto sin guía): animales en rehabilitación que hayan recibido
    // al menos un tratamiento de un especialista con determinada expertise.
    //
    // Se elige @Query + JPQL en lugar de Query Method porque la condición combina
    // dos caminos de navegación distintos sobre la misma entidad (rescueCase.status
    // y treatments.specialist.expertiseAreas.name), lo que haría el nombre del método
    // extremadamente largo y difícil de leer/mantener. Además, al atravesar la colección
    // treatments (1:N) y expertiseAreas (N:M) es fácil obtener animales duplicados si el
    // animal tiene varios tratamientos que cumplen la condición, por lo que se necesita
    // DISTINCT, algo que un Query Method no permite controlar explícitamente.
    @Query("""
            select distinct a
            from Animal a
            join a.rescueCase rc
            join a.treatments t
            join t.specialist s
            join s.expertiseAreas e
            where rc.status = :status
              and lower(e.name) = lower(:expertiseName)
            """)
    List<Animal> findInRehabilitationTreatedBySpecialistWithExpertise(
            @Param("status") RescueStatus status,
            @Param("expertiseName") String expertiseName);
}
