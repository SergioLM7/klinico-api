-- 0. Definir ENUMs
CREATE TYPE user_role AS ENUM ('SYSADMIN', 'MEDICO', 'JEFESERVICIO', 'ADMINISTRATIVO');
CREATE TYPE patient_status AS ENUM ('EXITUS', 'INGRESADO', 'ALTA');

-- 1. Crear tablas base (sin FKs circulares)
CREATE TABLE users (
                       user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       surname VARCHAR(100) NOT NULL,
                       role user_role NOT NULL, -- Uso del ENUM
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by UUID, -- Se añadirá el FK después
                       modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       modified_by UUID -- Se añadirá el FK después
);

CREATE TABLE services (
                          service_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(100) NOT NULL UNIQUE,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID REFERENCES users(user_id),
                          modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          modified_by UUID REFERENCES users(user_id)
);

-- 2. Añadir FK de service_id a users ahora que services existe
ALTER TABLE users ADD COLUMN service_id UUID REFERENCES services(service_id);
ALTER TABLE users ADD CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(user_id);
ALTER TABLE users ADD CONSTRAINT fk_modified_by FOREIGN KEY (modified_by) REFERENCES users(user_id);


-- 3. Crear tabla de Pacientes (Datos Administrativos)
CREATE TABLE patients (
                          patient_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          dni VARCHAR(9) NOT NULL UNIQUE,
                          name VARCHAR(50) NOT NULL,
                          surname VARCHAR(100) NOT NULL,
                          sex CHAR(1) not null,
                          birthdate DATE NOT NULL,
                          address TEXT,
                          contact_number VARCHAR(15),
                          relative_contact_number VARCHAR(15),
                          status patient_status not null DEFAULT 'INGRESADO',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID references users(user_id),
                          modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          modified_by UUID REFERENCES users(user_id),
                          version BIGINT DEFAULT 0,
                          constraint sex check (SEX in ('M', 'F'))
);

-- 4. Crear tabla de Ingresos (Admissions)
CREATE TABLE admissions (
                            admission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            patient_id UUID NOT NULL REFERENCES patients(patient_id),
                            service_id UUID NOT NULL REFERENCES services(service_id),
                            assigned_doctor_id UUID REFERENCES users(user_id),
                            discharge_date TIMESTAMP,
                            hospitalization_length INTEGER GENERATED ALWAYS AS (
                                CASE
                                WHEN discharge_date IS NOT NULL THEN
                                EXTRACT(DAY FROM (discharge_date - created_at))::INTEGER
            ELSE 0
        END
) stored,
    principal_diagnosis TEXT,
    basal_Barthel INT,
    chronic_treatment TEXT,
    allergies TEXT,
    medical_history TEXT,
    room_number INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID references users(user_id),
	modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID REFERENCES users(user_id),
    version BIGINT DEFAULT 0,
    -- Restricción P035: Fecha ingreso < Fecha alta
    CONSTRAINT check_dates CHECK (discharge_date IS NULL OR discharge_date > created_at)
);

-- 5. Crear tabla de Episodios (Pase de planta diario)
CREATE TABLE episodes (
                          episode_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          admission_id UUID NOT NULL REFERENCES admissions(admission_id),
                          doctor_id UUID NOT NULL REFERENCES users(user_id),
                          clinical_progress TEXT,
                          diagnosis TEXT,
                          braden_score INT,
                          cam_score BOOLEAN,
                          chads2_score INT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID references users(user_id),
                          modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          modified_by UUID REFERENCES users(user_id),
                          version BIGINT DEFAULT 0
);

-- 6. Restricciones a la hora de eliminar
-- En la tabla de Usuarios
ALTER TABLE users
    ADD CONSTRAINT fk_user_service
        FOREIGN KEY (service_id) REFERENCES services(service_id)
            ON DELETE SET NULL; -- Si el servicio se borra, el usuario sobrevive

-- En la tabla de Ingresos (Admissions)
ALTER TABLE admissions
    ADD CONSTRAINT fk_admission_patient
        FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
            ON DELETE RESTRICT; -- Prohibido borrar paciente con ingresos
ALTER TABLE admissions
    ADD CONSTRAINT fk_admission_service
        FOREIGN KEY (service_id) REFERENCES services(service_id)
            ON DELETE RESTRICT; -- Prohibido borrar servicio con ingresos

-- En la tabla de Episodios
ALTER TABLE episodes
    ADD CONSTRAINT fk_episode_admission
        FOREIGN KEY (admission_id) REFERENCES admissions(admission_id)
            ON DELETE RESTRICT; -- Prohibido borrar ingresos con episodios
ALTER TABLE episodes
    ADD CONSTRAINT fk_episode_doctor
        FOREIGN KEY (doctor_id) REFERENCES users(user_id)
            ON DELETE RESTRICT; -- Prohibido borrar médicos con episodios


    -- TRIGGER
-- 1. Creamos la función que actualizará la fecha de modificado automáticamente
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- 2. Aplicamos el trigger a cada tabla que tenga modified_at
CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_services_modtime BEFORE UPDATE ON services FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_patients_modtime BEFORE UPDATE ON patients FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_admissions_modtime BEFORE UPDATE ON admissions FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_episodes_modtime BEFORE UPDATE ON episodes FOR EACH ROW EXECUTE FUNCTION update_modified_column();