-- Script SQL para crear tablas de auditoría de Hibernate Envers
-- Este script debe ejecutarse después de schema.sql

-- Secuencia para generar IDs de revisión
CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

-- Tabla de control de revisiones
CREATE TABLE IF NOT EXISTS revinfo (
    rev INTEGER PRIMARY KEY DEFAULT nextval('revinfo_seq'),
    revtstmp BIGINT
);

-- Tabla de auditoría para admissions
CREATE TABLE IF NOT EXISTS admissions_aud (
    admission_id UUID NOT NULL,
    patient_id UUID,
    service_id UUID,
    assigned_doctor_id UUID,
    discharge_date TIMESTAMP,
    hospitalization_length INTEGER,
    principal_diagnosis TEXT,
    basal_barthel INT,
    chronic_treatment TEXT,
    allergies TEXT,
    medical_history TEXT,
    room_number INT,
    created_at TIMESTAMP,
    created_by UUID,
    modified_at TIMESTAMP,
    modified_by UUID,
    version BIGINT,
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    PRIMARY KEY (admission_id, rev)
);

-- Tabla de auditoría para episodes
CREATE TABLE IF NOT EXISTS episodes_aud (
    episode_id UUID NOT NULL,
    admission_id UUID,
    doctor_id UUID,
    clinical_progress TEXT,
    diagnosis TEXT,
    braden_score INT,
    cam_score BOOLEAN,
    chads2_score INT,
    created_at TIMESTAMP,
    created_by UUID,
    modified_at TIMESTAMP,
    modified_by UUID,
    version BIGINT,
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    PRIMARY KEY (episode_id, rev)
);
