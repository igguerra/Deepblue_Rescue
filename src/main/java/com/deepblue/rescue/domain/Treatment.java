package com.deepblue.rescue.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * TODO (Paso 27): Completar esta entidad tú mismo/a.
 *
 * Debe mapear la tabla "treatments" y contener:
 *   - Long id
 *   - Animal animal            -> @ManyToOne(fetch = FetchType.LAZY)
 *   - Specialist specialist    -> @ManyToOne(fetch = FetchType.LAZY)
 *   - LocalDateTime performedAt
 *   - TreatmentType type       -> @Enumerated(EnumType.STRING)
 *   - String description
 *
 * Pistas:
 *   - Usa @JoinColumn(name = "animal_id") y @JoinColumn(name = "specialist_id")
 *   - No olvides el constructor, getters y (si aplica) setters
 *   - Revisa el Paso 28: falta agregar las relaciones inversas en
 *     Animal.treatments y Specialist.treatments (ya están hechas en este
 *     esqueleto, pero verifica que el "mappedBy" coincida con el nombre
 *     de tu atributo aquí)
 */
@Entity
@Table(name = "treatments")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: mapear animal (@ManyToOne + @JoinColumn(name = "animal_id"))

    // TODO: mapear specialist (@ManyToOne + @JoinColumn(name = "specialist_id"))

    // TODO: mapear performedAt (@Column(name = "performed_at", nullable = false))

    // TODO: mapear type (@Enumerated(EnumType.STRING), @Column(nullable = false))

    // TODO: mapear description (@Column(columnDefinition = "TEXT"))

    protected Treatment() {
        // JPA
    }

    // TODO: crear constructor(Animal animal, Specialist specialist,
    //                          LocalDateTime performedAt, TreatmentType type,
    //                          String description)
    //       y asignar internamente animal/specialist (recuerda añadir "this"
    //       a las listas treatments de Animal y Specialist si quieres mantener
    //       ambos lados sincronizados)

    public Long getId() {
        return id;
    }

    // TODO: agregar el resto de getters
}
