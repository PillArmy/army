package io.army.example.coder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoderApp {

    static void main(String[] args) {
        final SpringApplication app = new SpringApplication(CoderApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }

}
