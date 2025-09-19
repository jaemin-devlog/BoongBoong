package org.hanseo.boongboong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
public class BoongBoongApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoongBoongApplication.class, args);
    }

}
