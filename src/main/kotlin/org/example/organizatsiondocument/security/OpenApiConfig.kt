package org.example.organizatsiondocument.security

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Organization Document API",
        version = "1.0.0",
        description = "Organization Document API hujjatlari",
        contact = Contact(
            name = "Navruzbek Gulomov",
            email = "navruzbek.dev@gmail.com",
            url = "https://github.com/developernavruzbek"
        )
    ),
    servers = [
        Server(url = "http://localhost:8080", description = "Local server")
    ],
    security = [
        SecurityRequirement(name = "bearerAuth")
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT authorization header using the Bearer scheme. Example: 'Bearer eyJhbGciOiJIUzI1NiIsIn...'",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
@Configuration
class OpenApiConfig
