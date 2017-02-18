/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import com.biqasoft.users.config.exceptionhandler.AuthEntryPointExceptionHandler;
import com.biqasoft.users.config.exceptionhandler.ExceptionTranslationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthEntryPointExceptionHandler authExceptionHandler;

    public SpringSecurityConfig() {
        super(true);
    } //disable auto configuration security filters

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(new ExceptionTranslationFilter(authExceptionHandler), BasicAuthenticationFilter.class);

        http.securityContext();
        http.anonymous();

        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/**").permitAll();
    }

}
