package com.biqasoft.users.config;

import org.apache.catalina.Context;
import org.apache.catalina.session.StandardManager;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by Nikita on 14.08.2016.
 */
@Configuration
public class ServerContext {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            servletContext.setSessionTrackingModes(Collections.emptySet()); // disable cookie generation Set-Cookie: JSESSIONID
        };
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addContextCustomizers(new TomcatContextCustomizer() {

            @Override
            public void customize(Context context) {
                if (context.getManager() instanceof StandardManager) {
                    ((StandardManager) context.getManager()).setPathname("");
                }
            }
        });
        return tomcat;
    }
}