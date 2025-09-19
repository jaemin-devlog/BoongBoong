// global/config/ThymeleafConfig.java
package org.hanseo.boongboong.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    public TemplateEngine templateEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(classLoaderTemplateResolver());
        return engine;
    }

    @Bean
    public ClassLoaderTemplateResolver classLoaderTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/mail/"); // templates/mail/verification.html
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCacheable(false);
        return resolver;
    }
}

