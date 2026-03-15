--INSERTS Services
INSERT INTO services (name) VALUES ('Medicina Interna');
INSERT INTO services (name) VALUES ('Geriatría');
INSERT INTO services (name) VALUES ('Cardiología');
INSERT INTO services (name) VALUES ('Neumología');
INSERT INTO services (name) VALUES ('Nefrología');
INSERT INTO services (name) VALUES ('Pediatría');
INSERT INTO services (name) VALUES ('Ginecología y Obstetricia');

--INSERTS users
INSERT INTO users (email, password_hash, name, surname, role)
VALUES (
           'admin@klinico.com',
           '$2a$10$8.UnVuG9shgE3zXG5H7uCe0W0W',
           'Sergio',
           'Admin',
           'SYSADMIN'
       );

INSERT INTO users (email, password_hash, name, surname, role)
VALUES (
           'administrativo@klinico.com',
           '$2a$12$adlgjYmC8GlNk4WqckmYvO.3Die9zg/x3ZyiW4HabDpqf0PUIrFAu',
           'Juan',
           'López',
           'ADMINISTRATIVO'
       );
INSERT INTO users (email, password_hash, name, surname, role, service_id)
VALUES (
           'medico1@klinico.com',
           '$2a$12$oyGKnR0frL4dUbw2SyUeVO3EQrXtMWdHZ3glMUXjnJ3LcKPeg8Sae',
           'Carlos',
           'García Ruíz',
           'MEDICO',
           'e0783110-21fe-4a06-8713-617883cc7438'
       );

INSERT INTO users (email, password_hash, name, surname, role, service_id)
VALUES (
           'medico2@klinico.com',
           '$2a$12$adlgjYmC8GlNk4WqckmYvO.3Die9zg/x3ZyiW4HabDpqf0PUIrFAu',
           'Ana',
           'Rodríguez López',
           'MEDICO',
           '56314568-0da9-4473-9913-d865a3d8451f'
       );

INSERT INTO users (email, password_hash, name, surname, role, service_id)
VALUES (
           'jefeservicio1@klinico.com',
           '$2a$12$kKGVt/QOYAlja3KVabEB3Oz6UnTTJZ7RzTolSBF4AGzZ0Wv5FDDBq',
           'Leocadio',
           'De la Morena Cenjor',
           'JEFESERVICIO',
           'e0783110-21fe-4a06-8713-617883cc7438'
       );

INSERT INTO users (email, password_hash, name, surname, role, service_id)
VALUES (
           'jefeservicio2@klinico.com',
           '$2a$12$U1/jQwu/slOw0LwM9hywue9mJZJaWvVrCIgYAKCLTPHPRZUsWWyxK',
           'Leocadio',
           'De la Morena Cenjor',
           'JEFESERVICIO',
           '56314568-0da9-4473-9913-d865a3d8451f'
       );

--INSERTS patients
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('12345678Z', 'Juan', 'Pérez', 'M', '1955-05-20', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('23456789A', 'María', 'García', 'F', '1962-11-03', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('34567890B', 'Carlos', 'López', 'M', '1948-02-14', 'ALTA');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('45678901C', 'Ana', 'Martínez', 'F', '1975-07-22', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('56789012D', 'Luis', 'Sánchez', 'M', '1959-09-10', 'ALTA');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('67890123E', 'Carmen', 'Fernández', 'F', '1968-03-30', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('78901234F', 'José', 'Gómez', 'M', '1952-12-05', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('89012345G', 'Lucía', 'Ruiz', 'F', '1980-06-18', 'ALTA');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('90123456H', 'Pedro', 'Díaz', 'M', '1945-01-27', 'INGRESADO');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('11223344J', 'Laura', 'Moreno', 'F', '1972-04-09', 'ALTA');
INSERT INTO patients (dni, name, surname, sex, birthdate, status)
VALUES ('22334455K', 'Miguel', 'Hernández', 'M', '1960-08-16', 'INGRESADO');