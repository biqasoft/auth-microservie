package com.biqasoft.users.config.exceptionhandler;

import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.InvalidRequestLocalizedException;
import com.biqasoft.microservice.i18n.MessageByLocaleService;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper o = new ObjectMapper();
    private final MessageByLocaleService messageByLocaleService;

    public ExceptionHandler(MessageByLocaleService messageByLocaleService) {
        this.messageByLocaleService = messageByLocaleService;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        if (throwable instanceof InvalidRequestLocalizedException) {
            InvalidRequestException ire = (InvalidRequestException) throwable;
            ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());

            try {
                ServerHttpResponse response = serverWebExchange.getResponse();
                DataBuffer buffer = response.bufferFactory().allocateBuffer();
                o.writeValue(buffer.asOutputStream(), error);

                response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
                return response.writeAndFlushWith(Mono.just(Mono.just(buffer))).flatMap(x -> Mono.empty());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        if (throwable instanceof BiqaAuthenticationLocalizedException) {
            BiqaAuthenticationLocalizedException ire = (BiqaAuthenticationLocalizedException) throwable;
            ErrorResource error = new ErrorResource(ire.getMessage(), messageByLocaleService.getMessage(ire.getMessage()));

            try {
                ServerHttpResponse response = serverWebExchange.getResponse();
                DataBuffer buffer = response.bufferFactory().allocateBuffer();
                o.writeValue(buffer.asOutputStream(), error);

                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeAndFlushWith(Mono.just(Mono.just(buffer))).flatMap(x -> Mono.empty());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        return Mono.error(throwable);
    }
}
////    @ExceptionHandler({InvalidRequestException.class})
//    protected ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
//        InvalidRequestException ire = (InvalidRequestException) e;
//
//        ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
//    }

//    @ExceptionHandler({InvalidRequestLocalizedException.class})
//    protected ResponseEntity<Object> handleInvalidRequestLocalizedException(RuntimeException e, WebRequest request) {
//        InvalidRequestLocalizedException ire = (InvalidRequestLocalizedException) e;
//
//        ErrorResource error = new ErrorResource("InvalidRequest", messageByLocaleService.getMessage(ire.getMessage()));
//        error.setIdErrorMessage(ire.getMessage());
//        error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
//    }
//
//    @ExceptionHandler({com.biqasoft.microservice.communicator.exceptions.InvalidRequestException.class})
//    protected ResponseEntity<Object> handleMicroserviceInvalidRequest(RuntimeException e, WebRequest request) throws IOException {
//        com.biqasoft.microservice.communicator.exceptions.InvalidRequestException ire = (com.biqasoft.microservice.communicator.exceptions.InvalidRequestException) e;
//
//        ClientHttpResponse microserviceResponse = ire.getClientHttpResponse();
//
//        ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());
//        error.setIdErrorMessage(ire.getMessage());
//        error.setEnglishErrorMessage(messageByLocaleService.getMessageEnglish(ire.getMessage()));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        return handleExceptionInternal(e, error, headers, microserviceResponse.getStatusCode(), request);
//    }
