package com.glinscravings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Glins Cravings - Main Application Entry Point
 *
 * Start this class to launch the backend server on port 8080.
 * Uses the MySQL profile configured in application.properties.
 *
 * Default admin account is hardcoded:
 *   Username: admin
 *   Password: 1234
 */
@SpringBootApplication
@EntityScan(basePackages = "com.glinscravings")
@EnableJpaRepositories(basePackages = "com.glinscravings")
public class Main {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        String port = context.getEnvironment().getProperty("local.server.port", context.getEnvironment().getProperty("server.port", "8080"));
        System.out.println("==============================================");
        System.out.println("  Glins Cravings Backend is RUNNING!");
        System.out.println("  API available at: http://localhost:" + port + "/api");
        System.out.println("  Default login: admin / 1234");
        System.out.println("  Active profile: mysql");
        System.out.println("==============================================");
    }
}