package com.sergio.klinico.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de OpenAPI 3 para la API Klinico.
 *
 * <p>Define los metadatos del contrato (título, versión, contacto) y el esquema de seguridad
 * JWT Bearer que se aplica a todos los endpoints protegidos. La UI de Swagger queda
 * disponible en {@code /swagger-ui/index.html} y la especificación JSON en
 * {@code /v3/api-docs}.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Klinico API",
                version = "1.0.0",
                description = "API REST para la gestión de ingresos hospitalarios, episodios clínicos, " +
                        "pacientes, usuarios y KPIs del servicio. Requiere autenticación JWT " +
                        "en todos los endpoints excepto en /api/v1/auth/login.",
                contact = @Contact(
                        name = "SM",
                        email = "sergiolillom@gmail.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor local de desarrollo")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Token JWT obtenido mediante POST /api/v1/auth/login. " +
                "Incluirlo en la cabecera Authorization con el formato: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
