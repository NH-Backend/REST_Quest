package io.nh_backend.rest_quest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RestQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestQuestApplication.class, args);
    }

}
