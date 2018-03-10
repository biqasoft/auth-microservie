package com.biqasoft.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties
public class ConfigurationPropertiesAuth {

    private boolean serverGrpcEnabled = true;
    private int serverGrpcPort;

    private boolean AuthSecurityGlobalRootEnable = false;

}
