package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // Paso 39 - Consulta A: buscar animal por animalCode
    // TODO: Optional<Animal> findByAnimalCode(String animalCode);

    // Paso 39 - Consulta B: buscar animales cuyo commonName contenga determinado texto,
    // ignorando mayúsculas/minúsculas
    // TODO: List<Animal> findByCommonNameContainingIgnoreCase(String text);

    // Paso 40: animales cuyo caso de rescate tenga determinado estado
    // Camino: Animal -> rescueCase -> status
    // TODO: List<Animal> findByRescueCaseStatus(RescueStatus status);

    // Paso 41: animales pertenecientes a un centro determinado
    // Camino: Animal -> RescueCase -> RescueCenter -> code
    // TODO: List<Animal> findByRescueCaseRescueCenterCode(String centerCode);

    // PARTE XIII (reto sin guía): animales en rehabilitación que hayan recibido
    // al menos un tratamiento de un especialista con determinada expertise.
    // Analiza si esto se resuelve con Query Method o si necesitas @Query + JPQL.
    // TODO: completar la firma y, si aplica, la consulta JPQL con @Query
}
