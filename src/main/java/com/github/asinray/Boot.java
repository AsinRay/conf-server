package com.github.asinray;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;


/**
 * iLook conf server.
 * @author Asin Liu
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableConfigServer
@SpringBootApplication(exclude ={SecurityAutoConfiguration.class} )
public class Boot {
    public static void main(String[] args) {
        SpringApplication.run(Boot.class, args);
    }
}
