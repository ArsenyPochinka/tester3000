package ru.vtb.tester3000;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Tester3000Application {

    public static void main(String[] args) {
        SpringApplication.run(Tester3000Application.class, args);
    }
}
