package com.deepblue.rescue;

import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.Expertise;
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");

    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private RescueCaseRepository rescueCaseRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    //PASO 48
    @Test
    void heritedMethodsWorkForRescueCenter() {
    RescueCenter center = new RescueCenter("DB-CAR", "Centro Cartagena", "Cartagena");

    RescueCenter saved = rescueCenterRepository.save(center);

    assertThat(saved.getId()).isNotNull();
    assertThat(rescueCenterRepository.existsById(saved.getId())).isTrue();
    assertThat(rescueCenterRepository.findById(saved.getId())).isPresent();
    assertThat(rescueCenterRepository.count()).isEqualTo(1);
    }

    //PASO 49
    @Test
    void oneRescueCenterHasManyRescueCases() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-SAM", "Centro Santa Marta", "Santa Marta"));

    RescueCase case1 = new RescueCase("CASE-001", LocalDate.now(), "Playa Salguero", RescueStatus.ADMITTED);
    RescueCase case2 = new RescueCase("CASE-002", LocalDate.now(), "Bahía Taganga", RescueStatus.ADMITTED);
    center.addCase(case1);
    center.addCase(case2);
    rescueCaseRepository.save(case1);
    rescueCaseRepository.save(case2);

    List<RescueCase> cases = rescueCaseRepository.findByRescueCenterCode("DB-SAM");

    assertThat(cases).hasSize(2);
    }

    //PASO 50
    @Test
    void rescueCaseHasOneAnimal() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-BAQ", "Centro Barranquilla", "Barranquilla"));
    RescueCase rescueCase = new RescueCase("CASE-010", LocalDate.now(), "Puerto Colombia", RescueStatus.ADMITTED);
    center.addCase(rescueCase);
    rescueCaseRepository.save(rescueCase);

    Animal animal = new Animal("AN-010", "Tortuga Verde", "Chelonia mydas", AnimalSex.FEMALE);
    rescueCase.assignAnimal(animal);
    animalRepository.save(animal);

    Optional<RescueCase> found = rescueCaseRepository.findByCaseCode("CASE-010");

    assertThat(found).isPresent();
    assertThat(found.get().getAnimal().getAnimalCode()).isEqualTo("AN-010");
    }

    //PASO 51
    @Test
    void animalCascadesMedicalRecord() {
    Animal animal = animalRepository.save(new Animal("AN-020", "Delfín Nariz de Botella", "Tursiops truncatus", AnimalSex.MALE));

    MedicalRecord record = new MedicalRecord(new BigDecimal("150.50"), "Estable", "Herida en aleta", "Buena recuperación");
    animal.assignMedicalRecord(record);
    animalRepository.save(animal);

    Optional<Animal> found = animalRepository.findByAnimalCode("AN-020");

    assertThat(found).isPresent();
    assertThat(found.get().getMedicalRecord()).isNotNull();
    assertThat(found.get().getMedicalRecord().getInitialCondition()).isEqualTo("Estable");
    }

    //PASO 52
    @Test
    void specialistHasManyExpertiseAreas() {
    Expertise trauma = expertiseRepository.save(new Expertise("Trauma"));
    Expertise nutrition = expertiseRepository.save(new Expertise("Nutrición"));

    Specialist specialist = new Specialist("SP-001", "Ana", "Gómez", "ana.gomez@deepblue.org");
    specialist.addExpertise(trauma);
    specialist.addExpertise(nutrition);
    specialistRepository.save(specialist);

    List<Specialist> found = specialistRepository.findActiveByExpertise("Trauma");

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getExpertiseAreas()).extracting(Expertise::getName)
            .containsExactlyInAnyOrder("Trauma", "Nutrición");
    }

    //PASO 53
    @Test
    void findsRescueCasesByStatus() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-030", "Centro Riohacha", "Riohacha"));
    RescueCase case1 = new RescueCase("CASE-030", LocalDate.now(), "Manaure", RescueStatus.IN_REHABILITATION);
    RescueCase case2 = new RescueCase("CASE-031", LocalDate.now(), "Manaure", RescueStatus.ADMITTED);
    center.addCase(case1);
    center.addCase(case2);
    rescueCaseRepository.save(case1);
    rescueCaseRepository.save(case2);

    List<RescueCase> found = rescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION);

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getCaseCode()).isEqualTo("CASE-030");
    }

    //PASO 54
    @Test
    void findsAnimalsByRescueCenterCode() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-040", "Centro Cartagena", "Cartagena"));
    RescueCase rescueCase = new RescueCase("CASE-040", LocalDate.now(), "Bocagrande", RescueStatus.ADMITTED);
    center.addCase(rescueCase);
    rescueCaseRepository.save(rescueCase);

    Animal animal = new Animal("AN-040", "Iguana Verde", "Iguana iguana", AnimalSex.MALE);
    rescueCase.assignAnimal(animal);
    animalRepository.save(animal);

    List<Animal> found = animalRepository.findByRescueCaseRescueCenterCode("DB-040");

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getAnimalCode()).isEqualTo("AN-040");
    }

    //PASO 55
    @Test
    void findsActiveSpecialistsByExpertise() {
    Expertise expertise = expertiseRepository.save(new Expertise("Cirugía"));
    Specialist specialist = new Specialist("SP-050", "Carlos", "Ruiz", "carlos.ruiz@deepblue.org");
    specialist.addExpertise(expertise);
    specialistRepository.save(specialist);

    List<Specialist> found = specialistRepository.findActiveByExpertise("Cirugía");

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getLastName()).isEqualTo("Ruiz");
    }

    //PASOS 56-58
    @Test
    void findsTreatmentsByAnimalAndDateRange() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-060", "Centro Taganga", "Taganga"));
    RescueCase rescueCase = new RescueCase("CASE-060", LocalDate.now(), "Taganga", RescueStatus.IN_REHABILITATION);
    center.addCase(rescueCase);
    rescueCaseRepository.save(rescueCase);

    Animal animal = new Animal("AN-060", "Pelícano", "Pelecanus occidentalis", AnimalSex.FEMALE);
    rescueCase.assignAnimal(animal);
    animalRepository.save(animal);

    Specialist specialist = specialistRepository.save(new Specialist("SP-060", "Laura", "Pérez", "laura.perez@deepblue.org"));

    Treatment t1 = new Treatment(animal, specialist, LocalDateTime.of(2026, 1, 10, 9, 0), TreatmentType.WOUND_CARE, "Curación inicial");
    Treatment t2 = new Treatment(animal, specialist, LocalDateTime.of(2026, 1, 15, 9, 0), TreatmentType.NUTRITION, "Suplemento nutricional");
    treatmentRepository.save(t1);
    treatmentRepository.save(t2);

    List<Treatment> byAnimal = treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId());
    assertThat(byAnimal).hasSize(2);
    assertThat(byAnimal.get(0).getPerformedAt()).isBefore(byAnimal.get(1).getPerformedAt());

    List<Treatment> byRange = treatmentRepository.findBetweenDates(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 12, 0, 0));
    assertThat(byRange).hasSize(1);
    assertThat(byRange.get(0).getDescription()).isEqualTo("Curación inicial");
    }

    @Test
    void rejectsDuplicatedAnimalCode() {
    animalRepository.saveAndFlush(new Animal("AN-070", "Tortuga Carey", "Eretmochelys imbricata", AnimalSex.MALE));

    Animal duplicate = new Animal("AN-070", "Otra Tortuga", "Otra especie", AnimalSex.FEMALE);

    assertThrows(DataIntegrityViolationException.class, () -> {
        animalRepository.saveAndFlush(duplicate);
    });
    }

    @Test
    void completeTurtleRescueScenario() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-080", "Centro Santa Marta", "Santa Marta"));

    RescueCase rescueCase = new RescueCase("CASE-080", LocalDate.now(), "Playa El Rodadero", RescueStatus.ADMITTED);
    center.addCase(rescueCase);
    rescueCaseRepository.save(rescueCase);

    Animal turtle = new Animal("AN-080", "Tortuga Verde", "Chelonia mydas", AnimalSex.FEMALE);
    rescueCase.assignAnimal(turtle);
    animalRepository.save(turtle);

    MedicalRecord record = new MedicalRecord(new BigDecimal("85.30"), "Deshidratada", "Golpe en caparazón", "Requiere hidratación urgente");
    turtle.assignMedicalRecord(record);
    animalRepository.save(turtle);

    Expertise expertise = expertiseRepository.save(new Expertise("Fauna Marina"));
    Specialist specialist = new Specialist("SP-080", "María", "Torres", "maria.torres@deepblue.org");
    specialist.addExpertise(expertise);
    specialistRepository.save(specialist);

    Treatment treatment = new Treatment(turtle, specialist, LocalDateTime.now(), TreatmentType.HYDRATION, "Hidratación intravenosa");
    treatmentRepository.save(treatment);

    rescueCase.setStatus(RescueStatus.IN_REHABILITATION);
    rescueCaseRepository.save(rescueCase);

    Optional<Animal> found = animalRepository.findByAnimalCode("AN-080");
    assertThat(found).isPresent();
    assertThat(found.get().getMedicalRecord().getInitialCondition()).isEqualTo("Deshidratada");
    assertThat(found.get().getRescueCase().getStatus()).isEqualTo(RescueStatus.IN_REHABILITATION);
    assertThat(found.get().getTreatments()).hasSize(1);
    assertThat(found.get().getTreatments().get(0).getSpecialist().getExpertiseAreas())
            .extracting(Expertise::getName)
            .contains("Fauna Marina");
    }

    @Test
    void findsAnimalsInRehabilitationTreatedBySpecialistWithTraumaExpertise() {
    RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-090", "Centro Palomino", "Palomino"));

    RescueCase rescueCase = new RescueCase("CASE-090", LocalDate.now(), "Palomino", RescueStatus.IN_REHABILITATION);
    center.addCase(rescueCase);
    rescueCaseRepository.save(rescueCase);

    Animal animal = new Animal("AN-090", "Manatí", "Trichechus manatus", AnimalSex.MALE);
    rescueCase.assignAnimal(animal);
    animalRepository.save(animal);

    Expertise trauma = expertiseRepository.save(new Expertise("Trauma"));
    Specialist specialist = new Specialist("SP-090", "Jorge", "Díaz", "jorge.diaz@deepblue.org");
    specialist.addExpertise(trauma);
    specialistRepository.save(specialist);

    Treatment treatment = new Treatment(animal, specialist, LocalDateTime.now(), TreatmentType.SURGERY, "Cirugía por trauma");
    treatmentRepository.save(treatment);

    List<Animal> found = animalRepository.findInRehabilitationTreatedBySpecialistWithExpertise(
            RescueStatus.IN_REHABILITATION, "Trauma");

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getAnimalCode()).isEqualTo("AN-090");
    }
}
