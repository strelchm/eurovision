package ru.strelchm.eurovision.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class OpenApiConfig implements WebMvcConfigurer {
    public static final String SUCCESS_MESSAGE_FIELD = "Success";

    private final Environment env;

    @Autowired
    public OpenApiConfig(Environment env) {
        this.env = env;
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("maps")
                .packagesToScan("ru.strelchm.eurovision.api")
                .pathsToMatch("/**")
//            .addOpenApiCustomiser(OpenApiCustomiser(openApi: OpenAPI ->{}))
                .build();
    }

    @Bean
    public OpenAPI mapsOpenAPIInfo() {
//        SecurityRequirement securityRequirement = new SecurityRequirement();
//        securityRequirement.addList("bearerAuth", new ArrayList<>());
        return new OpenAPI()
//                .components(new Components()
//                        .addSecuritySchemes("bearerAuth",
//                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
//                )
//                .security(Collections.singletonList(securityRequirement))
                .addServersItem(new Server()
//                        .url(env.getProperty("swagger.conf.base-url"))
                ).info(
                        new Info()
                                .title("Eurovision REST API")
                                .description("Eurovision REST API")
                                .version("v1.0")
                                .license(new License()
                                        .name("Apache License Version 2.0")
                                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                                )
                );
    }
}
