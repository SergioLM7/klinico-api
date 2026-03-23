<a id="readme-top"></a>
# Klinico 🏥 - API de Sistema de Gestión Clínica Hospitalaria

Klinico es una plataforma integral para la gestión de pacientes, admisiones y episodios clínicos, diseñada bajo estándares de alta disponibilidad y robustez técnica. 
Este proyecto se centra en la digitalización del flujo de trabajo médico, desde el ingreso del paciente hasta su alta, así como en la automatización de KPIs de interés para jefes de servicio.

El objetivo principal de esta API es proveer las entidades, operaciones y lógica de negocio necesarias para facilitar el pase de planta diario de los médicos de un hospital. No obstante, se ha desarrollado sobre una Arquitectura Hexagonal (Domain Driven Development) para desacoplar el dominio del canal de entrada, de forma que el mismo núcleo funcional pueda ser consumido tanto por una aplicación multiplataforma, como por un cliente web o de escritorio.


## 🚀 Características Principales

- **Gestión de Pacientes:** Registro, seguimiento de estados (Alta, Ingresado, Exitus) y trazabilidad completa.
- **Ciclo de Admisiones:** Control de ingresos por servicio, flujo y rol para asignación de habitaciones, y cálculo automático de estancia media (KPI).
- **Episodios Clínicos:** Evolución diaria documentada con escalas validadas (Braden, CHADS2, CAM) y con seguridad para evitar modificación de datos por cualquier otro usuario que no haya creado el episodio clínico.
- **Seguridad y auditoría:** Autenticación basada en JWT, seguridad por roles (Médico, Jefe de Servicio, Administrativo, Sys Admin) y auditoría automática de campos (`createdBy`, `createdAt`. `modifiedBy`, `modifiedAt`) junto a Hibernate Envers para dejar constancia de las diferentes modificaciones de las entidades de máxima relevancia.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## 🏗️ Arquitectura: Hexagonal (Ports & Adapters)

El proyecto implementa una **Arquitectura Hexagonal** para garantizar el desacoplamiento entre la lógica de negocio y las tecnologías externas:

- **Domain Layer:** Contiene modelos de dominio ricos --siguiendo los principios de DDD (Domain-Driven Design) para asegurar que las reglas de negocio estén centralizadas y protegidas dentro de las propias entidades (`Patient`, `Admission`, `Episode`, `User`)--, las interfaces de repositorio y las reglas de integridad.
- **Application Layer:** Servicios que orquestan cada uno de los casos de uso.
- **Infrastructure Layer:**
  - **Persistence:** Adaptadores JPA para PostgreSQL con MapStruct; Entities y JpaRepositories.
  - **Security:** Filtros JWT y configuración de Spring Security.
  - **Rest:** Controladores REST con limitación de acceso por roles.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## 🛠️ Stack Tecnológico

- **Backend:** Java 25, Spring Boot 4, Spring Data JPA, JUnit 6, Mockito.
- **Seguridad:** Spring Security + JWT (JSON Web Tokens)
- **Base de Datos:** PostgreSQL 17
- **Mapeo de Objetos y utils:** MapStruct, Lombok
- **Documentación:** JavaDocs, OpenAPI

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Estructura del proyecto
```text
com.sergio.klinico
├── application
│   └── services          # Lógica de orquestación y casos de uso
├── domain
│   ├── exceptions        # Excepciones de negocio (BusinessException)
│   ├── models            # Entidades puras de dominio
│   └── repositories      # Puertos de salida (Interfaces)
└── infrastructure
    ├── config            # Configuraciones generales de Spring Security
    ├── mappers           # Mapeos de MapStruct (Domain <-> Entity <-> DTO)
    ├── persistence       # Adaptadores de persistencia y JPA
    │   ├── adapters      # Implementación de los repositorios de dominio (Adapters)
    │   ├── configuration # Configuración específica de Auditoría (AuditorAware)
    │   ├── repositories  # Interfaces JpaRepository (Spring Data)
    │   └── entities      # Entidades JPA (@Entity) y AuditableEntity
    ├── rest              # Adaptadores de entrada (API REST)
    │   ├── advice        # GlobalExceptionHandler para gestión de errores
    │   ├── controllers   # Endpoints de la API
    │   └── dto           # Objetos de transferencia de datos
    │       ├── requests  # Payloads de entrada
    │       ├── responses # Estructura de salida (Admission, Episode, Patient, Login, Error)
    │       └── validations # Grupos de validación
    └── security          # Infraestructura de seguridad
        ├── filters       # Filtro de autenticación JWT
        └── services      # Gestión, validación y extracciones del Token
```


## 🛠️ Instalación y Configuración en Local
- 1. Requisitos Previos

Java 25 JDK o superior.

Gradle 9 (o usar el gradle-wrapper.jar)

Docker y Docker Compose (para la base de datos).

- 2. Clona el repo
   ```sh
   git clone https://github.com/SergioLM7/klinico-api/
   ```

- 3. Despliegue de la Base de Datos (Docker)

He incluido un archivo docker-compose.yml en la raíz para levantar PostgreSQL con un solo comando. Este contenedor ya incluye la persistencia de datos.

Comando para arrancar: 
 ```sh
 docker-compose up -d
```
 
- 4. Configuración de variables de entorno

Crea un archivo .env con tus variables de entorno. Se deja el .env.example de ejemplo.

- 5. Ejecución
     
```sh
gradle bootRun
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## 📘 Documentación

- API:
- JavaDocs: 

## 👨🏽‍💻 Contacto

**Sergio Lillo, Full Stack Software Developer**
<a href="https://www.linkedin.com/in/lillosergio/" target="_blank"> <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/LinkedIn_icon.svg/1200px-LinkedIn_icon.svg.png" width=30px, height=30px/></a> - sergiolillom@gmail.com

## © MIT License

Copyright (©) 2026, Sergio Lillo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

<p align="right">(<a href="#readme-top">back to top</a>)</p>
