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
            InvalidRequestLocalizedException ire = (InvalidRequestLocalizedException) throwable;
            ErrorResourceDto error = new ErrorResourceDto("InvalidRequest", ire.getMessage());

            try {
                ServerHttpResponse response = serverWebExchange.getResponse();
                DataBuffer buffer = response.bufferFactory().allocateBuffer();
                o.writeValue(buffer.asOutputStream(), error);

                response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
                response.getHeaders().add("Content-Type", "application/json");
                return response.writeAndFlushWith(Mono.just(Mono.just(buffer))).flatMap(x -> Mono.empty());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else if (throwable instanceof InvalidRequestException) {
            InvalidRequestException ire = (InvalidRequestException) throwable;
            ErrorResourceDto error = new ErrorResourceDto("InvalidRequest", ire.getMessage());

            try {
                ServerHttpResponse response = serverWebExchange.getResponse();
                DataBuffer buffer = response.bufferFactory().allocateBuffer();
                o.writeValue(buffer.asOutputStream(), error);

                response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
                response.getHeaders().add("Content-Type", "application/json");
                return response.writeAndFlushWith(Mono.just(Mono.just(buffer))).flatMap(x -> Mono.empty());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else if (throwable instanceof BiqaAuthenticationLocalizedException) {
            BiqaAuthenticationLocalizedException ire = (BiqaAuthenticationLocalizedException) throwable;
            ErrorResourceDto error = new ErrorResourceDto(ire.getMessage(), messageByLocaleService.getMessage(ire.getMessage()));

            try {
                ServerHttpResponse response = serverWebExchange.getResponse();
                DataBuffer buffer = response.bufferFactory().allocateBuffer();
                o.writeValue(buffer.asOutputStream(), error);

                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");
                return response.writeAndFlushWith(Mono.just(Mono.just(buffer))).flatMap(x -> Mono.empty());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        return Mono.error(throwable);
    }
}