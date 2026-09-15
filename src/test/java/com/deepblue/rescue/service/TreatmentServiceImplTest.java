package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    private Animal buildAnimal(RescueStatus status, LocalDate rescueDate) {
        RescueCase rescueCase = new RescueCase("RC-001", rescueDate, "Bahía Concha", status);
        Animal animal = new Animal("AN-001", "Tortuga verde", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        return animal;
    }

    private Specialist buildSpecialist(boolean active) {
        Specialist specialist = new Specialist("SP-001", "Ana", "Pérez", "ana@correo.com");
        specialist.setActive(active);
        return specialist;
    }

    private CreateTreatmentRequest buildRequest(LocalDateTime performedAt) {
        return new CreateTreatmentRequest(
                "AN-001", "SP-001", performedAt, TreatmentType.WOUND_CARE, "Limpieza de herida");
    }

    // TEST 5: tratamiento válido → llama a save()
    @Test
    void deberiaRegistrarTratamientoCuandoTodoEsValido() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION, LocalDate.now().minusDays(5));
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = buildRequest(LocalDateTime.now());

        Treatment savedTreatment = new Treatment(
                animal, specialist, request.performedAt(), request.type(), request.description());
        TreatmentResponse response = new TreatmentResponse(
                1L, "AN-001", "SP-001", request.performedAt(), TreatmentType.WOUND_CARE, "Limpieza de herida");

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SP-001")).thenReturn(Optional.of(specialist));
        when(treatmentRepository.save(any(Treatment.class))).thenReturn(savedTreatment);
        when(mapper.toResponse(savedTreatment)).thenReturn(response);

        TreatmentResponse result = service.register(request);

        assertThat(result.animalCode()).isEqualTo("AN-001");
        verify(treatmentRepository).save(any(Treatment.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoAnimalNoExiste() {
        when(animalRepository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());
        CreateTreatmentRequest request = buildRequest(LocalDateTime.now());

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // TEST 6: especialista inactivo → BusinessRuleException, NUNCA save()
    @Test
    void noDeberiaRegistrarTratamientoCuandoEspecialistaEstaInactivo() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION, LocalDate.now().minusDays(5));
        Specialist specialist = buildSpecialist(false);
        CreateTreatmentRequest request = buildRequest(LocalDateTime.now());

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SP-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // TEST 7: caso RELEASED → BusinessRuleException
    @Test
    void noDeberiaRegistrarTratamientoCuandoCasoYaFueLiberado() {
        Animal animal = buildAnimal(RescueStatus.RELEASED, LocalDate.now().minusDays(5));
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = buildRequest(LocalDateTime.now());

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SP-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // Extra: fecha de tratamiento antes de la fecha de rescate → BusinessRuleException
    @Test
    void noDeberiaRegistrarTratamientoCuandoFechaEsAnteriorAlRescate() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION, LocalDate.now());
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = buildRequest(LocalDateTime.now().minusDays(10));

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SP-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // Extra: findByAnimalCode
    @Test
    void deberiaRetornarTratamientosDeUnAnimal() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION, LocalDate.now().minusDays(5));
        Specialist specialist = buildSpecialist(true);
        Treatment treatment = new Treatment(
                animal, specialist, LocalDateTime.now(), TreatmentType.WOUND_CARE, "Limpieza");
        TreatmentResponse response = new TreatmentResponse(
                1L, "AN-001", "SP-001", treatment.getPerformedAt(), TreatmentType.WOUND_CARE, "Limpieza");

        when(treatmentRepository.findByAnimalIdOrderByPerformedAtAsc("AN-001"))
                .thenReturn(List.of(treatment));
        when(mapper.toResponse(treatment)).thenReturn(response);

        List<TreatmentResponse> result = service.findByAnimalCode("AN-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).animalCode()).isEqualTo("AN-001");
    }
}