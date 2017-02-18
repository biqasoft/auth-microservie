/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.config.exceptionhandler;

import com.biqasoft.microservice.i18n.MessageByLocaleService;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handle user basic auth error in API
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/10/2016
 *         All Rights Reserved
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageByLocaleService messageByLocaleService;

    @Autowired
    private CurrentUser currentUser;

    @ExceptionHandler({AuthenticationException.class})
    protected ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
        BiqaAuthenticationLocalizedException ire = (BiqaAuthenticationLocalizedException) e;

        ErrorResource error = new ErrorResource("Authentication Failed", messageByLocaleService.getMessage(ire.getMessage()));
        error.setIdErrorMessage(ire.getMessage());
        error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        try {
            error.setDomain(currentUser.getDomain().getDomain());
        } catch (Exception er) {
        }

        return handleExceptionInternal(e, error, headers, HttpStatus.UNAUTHORIZED, request);
    }

}
