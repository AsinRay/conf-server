package com.github.asinray.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;


import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.boot.context.event.ApplicationReadyEvent;


/**
 * Application init event handler to print/log the request mappings and spring 
 * security filters.
 * 
 * @author Asin Liu
 * @since 1.0.0
 */
@Component
public class ApplicationInitEvent{
    private Logger log = LoggerFactory.getLogger(ApplicationInitEvent.class);

    private static final List<String> shortens = new ArrayList<String>(){{
        add("com.github.asinray.");
        add("controller.");
        add("java.lang.");
        add("java.util.");
        add("javax.servlet.http.");
        add("controller.");
        add("public");
    }};

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Resource
    private ApplicationContext ac ;


    @EventListener(ApplicationReadyEvent.class)
    public void printApplicationStartInfo(){
        printSecurityFilters();
        printAllRequestMappingInfo();
    }

    public void printSecurityFilters() {
        FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
        List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
        if (list!= null && !list.isEmpty()) {
            printHeader("Filter Info");
        list.stream()
                .flatMap(chain -> chain.getFilters().stream())
                .forEach(filter -> System.out.println(filter.getClass()));
            System.out.println();
        }
    }

    public void printAllRequestMappingInfo() {
        AbstractHandlerMethodMapping<RequestMappingInfo> objHandlerMethodMapping =
                (AbstractHandlerMethodMapping<RequestMappingInfo>) ac.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> mapRet = objHandlerMethodMapping.getHandlerMethods();
        RequestMappingHandlerMapping mapping;
        if (mapRet != null && !mapRet.isEmpty()) {
            printMappings(mapRet);
        }
    }
    public static void printHeader(String header){
        String line = "=================================================" ;
        System.out.printf("\n*%s\n",line);
        System.out.printf("* %30s",header);
        System.out.printf("\n*%s\n",line);
    }


    private void printMappings(Map<RequestMappingInfo, HandlerMethod> map){

        printHeader("Mapping info");

        map.forEach((k,v)->{
            String ctl = v.toString();
            for (String s : shortens) {
                ctl = ctl.replaceAll(s, "");
            }
            ctl = ctl.replaceAll("org.springframework.cloud", "o.s.c");
            ctl = ctl.replaceAll("org.springframework.boot", "o.s.b");
            ctl = ctl.replaceAll("org.springframework", "o.s");

            String mappingInfo = k.toString().replace("methods=", "");
            System.out.printf(String.format("%-50s %s\n", mappingInfo, ctl));
        });
        System.out.println();
    }
}