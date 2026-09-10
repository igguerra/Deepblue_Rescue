package com.deepblue.rescue;

import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Paso 47: test de Flyway - comprobar que V1 y V2 se ejecutaron
    @Test
    void flywayEjecutoLasMigraciones() {
        List<String> versions = jdbcTemplate.queryForList(
                "select version from flyway_schema_history order by installed_rank",
                String.class
        );

        assertThat(versions).contains("1", "2");
    }

    // TODO Paso 48: test de métodos heredados (save, findById, existsById, count)
    // creando un RescueCenter "DB-CAR".

    // TODO Paso 49: test relación 1:N (RescueCenter -> 2 RescueCase)

    // TODO Paso 50: test relación 1:1 (RescueCase <-> Animal)

    // TODO Paso 51: test relación 1:1 (Animal <-> MedicalRecord) con cascade

    // TODO Paso 52: test relación N:M (Specialist <-> Expertise)

    // TODO Paso 53: test Query Method simple (buscar casos por status)

    // TODO Paso 54: test Query Method navegando relaciones (animales por centro)

    // TODO Paso 55: test JPQL de especialistas por expertise

    // TODO Pasos 56-58: crear tratamientos y probar Query Method + JPQL por intervalo

    // TODO Paso 59: test de constraint UNIQUE con saveAndFlush()
    //       (esperar DataIntegrityViolationException)

    // TODO Parte XII: reto integrador completo (escenario de la tortuga)

    // TODO Parte XIII: reto sin guía (animales en rehabilitación + expertise Trauma)
}
