package com.lollito.fm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("fm-public")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI fmOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Football Manager API")
                        .description("Football Manager online game API")
                        .version("v0.0.1"));
    }
}
