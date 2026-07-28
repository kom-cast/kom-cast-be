package com.komcast.be.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url:}")
    private String customServerUrl;

    @Bean
    public OpenAPI openAPI() {
        String securityHeaderName = "X-User-Id";

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(securityHeaderName)
                .description("해커톤 유저 식별용 헤더 (기본값: 1)");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securityHeaderName);

        List<Server> servers = new ArrayList<>();

        if (customServerUrl != null && !customServerUrl.isBlank()) {
            servers.add(new Server().url(customServerUrl).description("Production Deployment Server"));
        }

        // Relative URL server automatically points Swagger UI to the current hosting server IP/domain
        servers.add(new Server().url("/").description("Current Host Server"));

        return new OpenAPI()
                .info(new Info()
                        .title("Kom-Cast REST API Specification")
                        .description("Kom-Cast 개인화 AI 브리핑 서비스를 위한 백엔드 API 명세서입니다.")
                        .version("v1.0.0"))
                .servers(servers)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(securityHeaderName, securityScheme));
    }
}
