package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    // No se requieren consultas adicionales por ahora.
    // Si lo necesitas, puedes agregar Query Methods aquí más adelante.
}
