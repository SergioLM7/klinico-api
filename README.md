# Klinico 🏥 - API de Sistema de Gestión Clínica Hospitalaria

Klinico es una plataforma integral para la gestión de pacientes, admisiones y episodios clínicos, diseñada bajo estándares de alta disponibilidad y robustez técnica. 
Este proyecto se centra en la digitalización del flujo de trabajo médico, desde el ingreso del paciente hasta su alta.

## 🚀 Características Principales

- **Gestión de Pacientes:** Registro, seguimiento de estados (Alta, Ingresado, Exitus) y trazabilidad completa.
- **Ciclo de Admisiones:** Control de ingresos por servicio, asignación de habitaciones y cálculo automático de estancia media (KPI).
- **Episodios Clínicos:** Evolución diaria documentada con escalas validadas (Braden, CHADS2, CAM).
- **Seguridad Robusta:** Autenticación basada en JWT, seguridad por roles (Médico, Jefe de Servicio, Administrativo, Sys Admin) y auditoría automática de campos (`createdBy`, `createdAt`. `modifiedBy`. `modifiedAt`).

## 🏗️ Arquitectura: Hexagonal (Ports & Adapters)

El proyecto implementa una **Arquitectura Hexagonal** para garantizar el desacoplamiento entre la lógica de negocio y las tecnologías externas:

- **Domain Layer:** Contiene las entidades de negocio (`Patient`, `Admission`, `Episode`, `User`), las interfaces de repositorio y las reglas de integridad.
- **Application Layer:** Servicios que orquestan los casos de uso.
- **Infrastructure Layer:**
  - **Persistence:** Adaptadores JPA para PostgreSQL con MapStruct; Entities y JpaRepositories.
  - **Security:** Filtros JWT y configuración de Spring Security.
  - **Rest:** Controladores REST documentados.


## 🛠️ Stack Tecnológico

- **Backend:** Java 25, Spring Boot 4, Spring Data JPA.
- **Seguridad:** Spring Security + JWT (JSON Web Tokens).
- **Base de Datos:** PostgreSQL 17.
- **Mapeo de Objetos:** MapStruct.
- **Documentación:** Swagger/OpenAPI.

## 🛠️ Instalación y Configuración en Local
- 1. Requisitos Previos

Java 25 JDK o superior.

Gradle 9 (o usar el gradle-wrapper.jar)

Docker y Docker Compose (para la base de datos).

- 2. Despliegue de la Base de Datos (Docker)

He incluido un archivo docker-compose.yml en la raíz para levantar PostgreSQL con un solo comando. Este contenedor ya incluye la persistencia de datos.

Comando para arrancar: 
 ``docker-compose up -d``
- 3. Configuración de variables de entorno

Crea un archivo .env. Se deja el .env.example de ejemplo.

- 4. Ejecución
  
