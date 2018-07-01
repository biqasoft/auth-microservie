/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users;

import com.biqasoft.microservice.communicator.interfaceimpl.annotation.EnableMicroserviceCommunicator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(value = "com.biqasoft", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.biqasoft.entity"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.biqasoft.auth.GatewayAuthenticationProvider")})
@EnableAspectJAutoProxy
@EnableAutoConfiguration(exclude = {
        MongoDataAutoConfiguration.class, WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class
})
@EnableMicroserviceCommunicator
@EnableScheduling
public class StartApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }
}
