package com.biqasoft.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationPropertiesAuth {

    @Data
    @ConfigurationProperties(prefix = "server.grpc")
    @Configuration
    public static class GrpcProps {

        private boolean enabled = true;
        private int port;

    }

    @Data
    @ConfigurationProperties(prefix = "biqa.auth.limits.interval.fail")
    @Configuration
    public static class IntervalProps {

        private boolean enable = true;
        private int times;

    }
//
//    @Data
//    @ConfigurationProperties(prefix = "biqa.auth.limits.interval")
//    @Configuration
//    public static class IntervalPropsAll {
//
//        private boolean enable = true;
//        private int times;
//
//    }

}
