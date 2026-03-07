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
                          constraint sex check (SEX in ('M', 'F'))
);

-- 4. Crear tabla de Ingresos (Admissions)
CREATE TABLE admissions (
                            admission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            patient_id UUID NOT NULL REFERENCES patients(patient_id),
                            service_id UUID NOT NULL REFERENCES services(service_id),
                            assigned_doctor_id UUID REFERENCES users(user_id),
                            admission_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            discharge_date TIMESTAMP,
                            hospitalization_length INT,
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
    -- Restricción P035: Fecha ingreso < Fecha alta
                            CONSTRAINT check_dates CHECK (discharge_date IS NULL OR discharge_date > admission_date)
);

-- 5. Crear tabla de Episodios (Pase de planta diario)
CREATE TABLE episodes (
                          episode_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          admission_id UUID NOT NULL REFERENCES admissions(admission_id),
                          doctor_id UUID NOT NULL REFERENCES users(user_id),
                          episode_date DATE NOT NULL DEFAULT CURRENT_DATE,
                          clinical_progress TEXT,
                          diagnosis TEXT,
                          braden_score INT,
                          cam_score BOOLEAN,
                          chads2_score INT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID references users(user_id),
                          modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          modified_by UUID REFERENCES users(user_id)
);