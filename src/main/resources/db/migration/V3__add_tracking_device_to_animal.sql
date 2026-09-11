-- ============================================================
-- V3__add_tracking_device_to_animal.sql
-- Agrega dispositivo de rastreo GPS opcional a los animales
-- ============================================================

ALTER TABLE animals
    ADD COLUMN tracking_device_code VARCHAR(50);

ALTER TABLE animals
    ADD CONSTRAINT uk_animals_tracking_device_code UNIQUE (tracking_device_code);