package com.dto.project.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 로컬 파일 업로드 허용을 위한 임시 조치
    // TODO: 실 서비스 시 삭제
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-url-prefix}")
    private String uploadUrlPrefix;

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
        registry.addMapping("/**") // 모든 API 경로에 대해
                .allowedOrigins("http://localhost:5173") // 프론트엔드 주소 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키 및 인증 정보 허용
    }
}