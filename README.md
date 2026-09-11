# DeepBlue Rescue

DeepBlue Rescue es un laboratorio de persistencia con Spring Boot, JPA/Hibernate y PostgreSQL que modela el funcionamiento de una red de centros de rescate de fauna marina. La plataforma permite registrar casos de rescate, hacer seguimiento a los animales rescatados (ficha médica, tratamientos) y administrar el equipo de especialistas que los atiende junto con sus áreas de experiencia.


# Modelo de datos

El dominio está compuesto por las siguientes entidades:

  -**RescueCenter:** centro de rescate (código, nombre, ciudad).
  
  -**RescueCase:** caso de rescate (código, fecha, ubicación, estado).
  
  -**Animal:** animal rescatado (código, nombre común, nombre científico, sexo, dispositivo de rastreo).
  
  -**MedicalRecord:** ficha médica inicial de un animal (peso, condición, lesiones, observaciones).
  
  -**Specialist:** especialista del equipo (código profesional, nombre, email, estado activo).
  
  -**Expertise:** área de experiencia de un especialista (ej. Trauma, Nutrición).
  
  -**Treatment:** tratamiento aplicado a un animal por un especialista (fecha, tipo, descripción).

**Enums de apoyo:** AnimalSex, RescueStatus, TreatmentType.


# Relaciones

  -**RescueCenter → RescueCase (1:N):** un centro tiene muchos casos (RescueCenter.rescueCases, mappedBy = "rescueCenter").
  
  -**RescueCase ↔ Animal (1:1):** un caso tiene un único animal asociado, con cascade y orphanRemoval desde RescueCase.
  
  -**Animal ↔ MedicalRecord (1:1):** un animal tiene una única ficha médica, con cascade y orphanRemoval desde Animal.
  
  -**Animal → Treatment (1:N):** un animal puede tener varios tratamientos (mappedBy = "animal").
  
  -**Specialist → Treatment (1:N):** un especialista puede realizar varios tratamientos (mappedBy = "specialist").
  
  -**Specialist ↔ Expertise (N:M):** un especialista puede tener varias áreas de experiencia y viceversa, mediante la tabla intermedia specialist_expertise.

Todas las relaciones usan FetchType.LAZY para evitar cargas innecesarias.


# Instrucciones para ejecutar

**Requisitos:** JDK 21, Maven, Docker (para Testcontainers) y, opcionalmente, una instancia local de PostgreSQL si se desea correr la aplicación fuera de pruebas.

    bash
    mvn clean install

La configuración de conexión a base de datos se define en *src/main/resources/application.yml*, con valores por defecto que pueden sobreescribirse mediante las variables de entorno DB_URL, DB_USER y DB_PASSWORD.


# Instrucciones para ejecutar tests

Los tests de integración usan Testcontainers, por lo que Docker debe estar corriendo antes de ejecutarlos.

    bash
    mvn test

    
# Testcontainers

El proyecto usa *spring-boot-testcontainers* junto con *testcontainers-postgresql* para levantar una instancia real de PostgreSQL en un contenedor Docker durante los tests de integración, en lugar de usar una base de datos en memoria (como H2).

En *PersistenceIntegrationTest*, el contenedor se declara así:

    java
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");
                  
**@Testcontainers** habilita el ciclo de vida automático del contenedor durante la clase de test.

**@Container** marca el campo como un contenedor administrado por Testcontainers.

**@ServiceConnection** conecta automáticamente el DataSource de Spring Boot al contenedor levantado, sin necesidad de configurar manualmente la URL, usuario o contraseña.

Esto permite validar el comportamiento real de *JPA/Hibernate* contra PostgreSQL (tipos de datos, constraints, dialecto SQL) en cada ejecución de test, garantizando mayor fidelidad que una base de datos en memoria.


# Query Methods implementados

  -RescueCenterRepository.findByCode(String code): busca un centro por su código.
  
  -RescueCaseRepository.findByCaseCode(String caseCode): busca un caso por su código.
  
  -RescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus status): casos según estado, ordenados por fecha de rescate ascendente.
  
  -RescueCaseRepository.findByRescueCenterCode(String centerCode): casos de un centro determinado, navegando rescueCenter.code.
  
  -RescueCaseRepository.findByRescueDateAfterOrderByRescueDateDesc(LocalDate date): casos posteriores a una fecha, del más reciente al más antiguo.
  
  -AnimalRepository.findByAnimalCode(String animalCode): busca un animal por su código.
  
  -AnimalRepository.findByCommonNameContainingIgnoreCase(String text): animales cuyo nombre común contiene un texto, sin distinguir mayúsculas/minúsculas.
  
  -AnimalRepository.findByRescueCaseStatus(RescueStatus status): animales cuyo caso de rescate tiene determinado estado.
  
  -AnimalRepository.findByRescueCaseRescueCenterCode(String centerCode): animales pertenecientes a un centro determinado.
  
  -ExpertiseRepository.findByNameIgnoreCase(String name): busca un área de experiencia por nombre, sin distinguir mayúsculas/minúsculas.
  
  -TreatmentRepository.findByAnimalIdOrderByPerformedAtAsc(Long animalId): tratamientos de un animal, ordenados cronológicamente.


# Consultas JPQL implementadas
  -SpecialistRepository.findActiveByExpertise(String expertiseName): especialistas activos con determinada experiencia, ordenados por apellido.
  
  -TreatmentRepository.findBetweenDates(LocalDateTime start, LocalDateTime end): tratamientos realizados entre dos fechas, ordenados cronológicamente.
  
  -TreatmentRepository.findByCenterCode(String centerCode): tratamientos de animales pertenecientes a un centro determinado.
  
  -TreatmentRepository.findBySpecialistExpertise(String expertiseName): tratamientos realizados por especialistas con determinada experiencia (relación N:M).
  
  -AnimalRepository.findInRehabilitationTreatedBySpecialistWithExpertise(RescueStatus status, String expertiseName): animales con determinado estado que hayan recibido al menos un tratamiento de un especialista con cierta experiencia. Usa @Query con DISTINCT en lugar de Query Method porque combina dos caminos de navegación distintos sobre la misma entidad y requiere evitar duplicados al atravesar colecciones 1:N y N:M.


# Tests de integración

*PersistenceIntegrationTest* corre contra una base de datos PostgreSQL real levantada con Testcontainers y está anotada con **@Transactional**, por lo que cada test revierte sus cambios automáticamente al terminar, sin dejar datos residuales para el siguiente.

Los 12 tests implementados son:

-heritedMethodsWorkForRescueCenter: métodos heredados de JpaRepository (save, findById, existsById, count).

-oneRescueCenterHasManyRescueCases: relación 1:N entre RescueCenter y RescueCase.

-rescueCaseHasOneAnimal: relación 1:1 entre RescueCase y Animal.

-animalCascadesMedicalRecord: relación 1:1 con cascade entre Animal y MedicalRecord.

-specialistHasManyExpertiseAreas: relación N:M entre Specialist y Expertise.

-findsRescueCasesByStatus: Query Method simple filtrando casos por estado.

-findsAnimalsByRescueCenterCode: Query Method navegando relaciones (animales por centro).

-findsActiveSpecialistsByExpertise: consulta JPQL de especialistas por experiencia.

-findsTreatmentsByAnimalAndDateRange: Query Method y JPQL combinados sobre tratamientos, por animal y por rango de fechas.

-rejectsDuplicatedAnimalCode: constraint UNIQUE sobre animalCode, verificado con saveAndFlush() y DataIntegrityViolationException.

-completeTurtleRescueScenario: escenario integrador completo (caso de rescate → animal → ficha médica → tratamiento → especialista).

-findsAnimalsInRehabilitationTreatedBySpecialistWithTraumaExpertise: consulta combinada de animales en rehabilitación tratados por especialistas con una experiencia determinada.
