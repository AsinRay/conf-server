package com.forotc.conf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;


/**
 * iLook conf server.
 * @author Asin Liu
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigureServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigureServer.class, args);
    }
}
