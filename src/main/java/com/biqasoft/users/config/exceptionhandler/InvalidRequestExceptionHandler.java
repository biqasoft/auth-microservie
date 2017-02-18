/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config.exceptionhandler;

import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.InvalidRequestLocalizedException;
import com.biqasoft.common.exceptions.dto.ErrorResource;
import com.biqasoft.microservice.i18n.MessageByLocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InvalidRequestExceptionHandler extends ResponseEntityExceptionHandler{

    @Autowired
    private MessageByLocaleService messageByLocaleService;

    @ExceptionHandler({InvalidRequestException.class})
    protected ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
        InvalidRequestException ire = (InvalidRequestException) e;

        ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({InvalidRequestLocalizedException.class})
    protected ResponseEntity<Object> handleInvalidRequestLocalizedException(RuntimeException e, WebRequest request) {
        InvalidRequestLocalizedException ire = (InvalidRequestLocalizedException) e;

        ErrorResource error = new ErrorResource("InvalidRequest", messageByLocaleService.getMessage(ire.getMessage()));
        error.setIdErrorMessage(ire.getMessage());
        error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({com.biqasoft.microservice.communicator.exceptions.InvalidRequestException.class})
    protected ResponseEntity<Object> handleMicroserviceInvalidRequest(RuntimeException e, WebRequest request) throws IOException {
        com.biqasoft.microservice.communicator.exceptions.InvalidRequestException ire = (com.biqasoft.microservice.communicator.exceptions.InvalidRequestException) e;

        ClientHttpResponse microserviceResponse = ire.getClientHttpResponse();

        ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());
        error.setIdErrorMessage(ire.getMessage());
        error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(e, error, headers, microserviceResponse.getStatusCode(), request);
    }

}