package org.hanseo.boongboong.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under local 'uploads' dir via /files/**
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/");
    }
}

