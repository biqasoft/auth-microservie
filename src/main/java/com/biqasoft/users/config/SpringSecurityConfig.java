/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

@Configuration
public class SpringSecurityConfig {

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private ServerSecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Disable default security.
//        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();
        http.logout().disable();

        // Add custom security.
        http.authenticationManager(this.authenticationManager);
        http.securityContextRepository(this.securityContextRepository);


        // Disable authentication for `/auth/**` routes.
//        http.authorizeExchange().pathMatchers("/auth/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();

        return http.build();
    }

//    @Override
//    protected void configure(ServerHttpSecurity http) throws Exception {
//        http.addFilterAfter(new ExceptionTranslationFilter(authExceptionHandler), BasicAuthenticationFilter.class);
//
//        http.securityContext();
//        http.anonymous();
//
//        http.authorizeRequests()
//                .antMatchers(HttpMethod.OPTIONS).permitAll()
//                .antMatchers("/**").permitAll();
//    }

}
