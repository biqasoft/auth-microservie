package com.biqasoft.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class WebFilterConfiuration {

    @Bean
    WebFilter webFilter () {
        return (exchange, chain) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Access-Control-Max-Age", "3600");
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");

            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                response.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
                response.getHeaders().add("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Date, X-Date, Authorization");
                response.getHeaders().add("X-Frame-Options", "DENY"); //SAMEORIGIN

                // if http request is options - do not process filters chain after
                // because we have user authentication filter and it will fail
                // with exception
                return Mono.empty();
            }

            // CORS, allow all use our API via Ajax
            List<String> origin = exchange.getRequest().getHeaders().get("Origin");
            if (origin != null && origin.size() == 1) {
                response.getHeaders().add("Access-Control-Allow-Origin", origin.get(0));
            }
            return chain.filter(exchange);
        };
    }

}
