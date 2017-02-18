/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.config.exceptionhandler;

import com.biqasoft.microservice.i18n.MessageByLocaleService;
import com.biqasoft.common.exceptions.dto.ErrorResource;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handle user basic auth error in API
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/10/2016
 *         All Rights Reserved
 */
@Component
public class AuthEntryPointExceptionHandler extends BasicAuthenticationEntryPoint {

    @Autowired
    private MessageByLocaleService messageByLocaleService;

    @Autowired
    private CurrentUser currentUser;

    private final String realmName = "biqa";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        ErrorResource error;
        if (httpServletResponse.isCommitted()){
            return;
        }

        if (e instanceof BiqaAuthenticationLocalizedException) {
            BiqaAuthenticationLocalizedException ire = (BiqaAuthenticationLocalizedException) e;
            error = new ErrorResource("Authentication Failed", messageByLocaleService.getMessage(ire.getMessage()));
            error.setIdErrorMessage(ire.getMessage());
            error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));
            try {
                error.setDomain(currentUser.getDomain().getDomain());
            } catch (Exception er) {
            }

        } else {
            error = new ErrorResource("Authentication Failed", e.getMessage());
            error.setIdErrorMessage(e.getMessage());
            error.setEnglishErrorMessage(e.getMessage());
        }

        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.setStatus(401);
        httpServletResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + this.realmName + "\"");

//        httpServletResponse.getOutputStream().write(objectMapper.writeValueAsBytes(error));
        httpServletResponse.getWriter().append(objectMapper.writeValueAsString(error)).flush();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.realmName, "realmName must be specified");
    }

}
