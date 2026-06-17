package com.dto.project.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-url-prefix}")
    private String uploadUrlPrefix;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        String handlerPattern = uploadUrlPrefix.endsWith("/")
                ? uploadUrlPrefix + "**"
                : uploadUrlPrefix + "/**";

        registry.addResourceHandler(handlerPattern)
                .addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl) // localhost 대신 주입받은 주소 설정
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}