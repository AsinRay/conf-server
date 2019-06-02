package com.github.asinray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * iLook conf server.
 * @author Asin Liu
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableConfigServer
@SpringBootApplication(exclude ={SecurityAutoConfiguration.class} )
public class Boot {
    private static final Logger log = LoggerFactory.getLogger(Boot.class);
    public static void main(String[] args) {
        //SpringApplication.run(Boot.class, args);
        ConfigurableApplicationContext context = SpringApplication.run(Boot.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        log.info("spring.profiles.active = " + environment.getProperty("spring.profiles.active"));
        String appName = environment.getProperty("spring.application.name");
        String port = environment.getProperty("server.port");
        String contextPath = environment.getProperty("server.servlet.context-path");
        String jdbcURL = environment.getProperty("datasouce.url");
        String jdbcUser = environment.getProperty("username");
        log.info("appName = " + appName);
        log.info("server.port = " + port);
        log.info("contextPath = " + contextPath);
        log.info("jdbcURL = " + jdbcURL );
        log.info("jdbcUser = " + jdbcUser);
        log.info("hello = " + environment.getProperty("hello"));

        log.info("http://localhost:" + port + contextPath + "/hello?name=");
        log.info("http://localhost:" + port + contextPath + "/otherFile");
    }
}
