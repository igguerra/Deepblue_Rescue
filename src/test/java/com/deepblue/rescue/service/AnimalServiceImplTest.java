package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AnimalMapper animalMapper;

    @InjectMocks
    private AnimalServiceImpl animalService;

    private RescueCase buildRescueCase(RescueStatus status) {
        RescueCase rescueCase = new RescueCase(
                "RC-001", LocalDate.now(), "Bahía Concha", status);
        return rescueCase;
    }

    private Animal buildAnimal(RescueCase rescueCase) {
        Animal animal = new Animal("AN-001", "Tortuga verde", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        return animal;
    }

    @Test
    void deberiaRetornarAnimalCuandoExiste() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.IN_REHABILITATION);
        Animal animal = buildAnimal(rescueCase);

        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Tortuga verde", "Chelonia mydas",
                AnimalSex.FEMALE, "RC-001", RescueStatus.IN_REHABILITATION);

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(animalMapper.toResponse(animal)).thenReturn(response);

        AnimalResponse result = animalService.findByCode("AN-001");

        assertThat(result.animalCode()).isEqualTo("AN-001");
    }

    @Test
    void deberiaLanzarExcepcionCuandoAnimalNoExiste() {
        when(animalRepository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.findByCode("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deberiaPermitirTratamientoCuandoEstaEnRehabilitacion() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.IN_REHABILITATION);
        Animal animal = buildAnimal(rescueCase);

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isTrue();
    }

    @Test
    void noDeberiaPermitirTratamientoCuandoEstaLiberado() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.RELEASED);
        Animal animal = buildAnimal(rescueCase);

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean result = animalService.canReceiveTreatment("AN-001");

        assertThat(result).isFalse();
    }

    @Test
    void deberiaRetornarListaDeAnimalesEnRehabilitacion() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.IN_REHABILITATION);
        Animal animal = buildAnimal(rescueCase);

        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Tortuga verde", "Chelonia mydas",
                AnimalSex.FEMALE, "RC-001", RescueStatus.IN_REHABILITATION);

        when(animalRepository.findByRescueCaseStatus(RescueStatus.IN_REHABILITATION))
                .thenReturn(List.of(animal));
        when(animalMapper.toResponse(animal)).thenReturn(response);

        List<AnimalResponse> result = animalService.findAnimalsInRehabilitation();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).animalCode()).isEqualTo("AN-001");
    }
}