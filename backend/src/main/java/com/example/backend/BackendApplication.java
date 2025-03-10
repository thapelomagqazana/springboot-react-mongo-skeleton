package com.example.backend;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String port = dotenv.get("SERVER_PORT", "8080");

        new SpringApplicationBuilder(BackendApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }

}
