package com.github.asinray.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Properties;

import static com.github.asinray.service.ApiService.TOKEN_PATTERN;

/**
 * ApiServiceTest
 */


@RunWith(SpringRunner.class)
@ContextConfiguration(classes ={ApiServiceTest.TestContextConfiguration.class,ApiService.class})
public class ApiServiceTest {

    @Configuration
    static class TestContextConfiguration{
        @Bean
        public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
            PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("config/application.properties"));
            Properties properties = new Properties();
            properties.setProperty("security.aes.encrypt.key", "0xff90fa89fd82xd");
            properties.setProperty("security.aes.encrypt.iv", "0xfff0421xea1ray");
            ppc.setProperties(properties);
            ppc.setIgnoreResourceNotFound(true);
            return ppc;
        }
    }

    @MockBean
    InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Resource
    ApiService apiService;

    @Test
    public void genSecTokenTest(){

        String s = apiService.genSecToken();
        System.out.println(s);
        boolean b = TOKEN_PATTERN.matcher(s).matches();
        Assert.assertTrue(b);
    }
    
}