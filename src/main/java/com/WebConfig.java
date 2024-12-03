package com;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080") // Vue.js 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true); // 자격 증명 허용 (쿠키 포함)
    }
}
