/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResultDto;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.AuthServerInternalAuth;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 7/22/2016
 * All Rights Reserved
 */
@Service
public class RequestAuthenticateService implements ServerSecurityContextRepository, ReactiveAuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(RequestAuthenticateService.class);

    private final AuthFailedLimit authFailedLimit;
    private final List<AuthChainFilter> authChainFilters;

    @Autowired
    public RequestAuthenticateService(AuthFailedLimit authFailedLimit, List<AuthChainFilter> authChainFilters) {
        this.authFailedLimit = authFailedLimit;

        if (CollectionUtils.isEmpty(authChainFilters)) {
            throw new IllegalArgumentException("AuthChainFilter can not be empty");
        }
        this.authChainFilters = authChainFilters;

        logger.info("The following filter chain (and order) will be used: ");

        for (AuthChainFilter authChainFilter : this.authChainFilters) {
            logger.info(authChainFilter.getName() + " - " + authChainFilter.getDescription());
        }
    }

    private AuthenticateRequest tryAuthenticateResponseAsToken(@NotNull AuthenticateRequest authenticateRequest) {

        // if we already have username and password - skip processing as token
        if (authenticateRequest != null && !StringUtils.isEmpty(authenticateRequest.getUsername()) && !StringUtils.isEmpty(authenticateRequest.getPassword())) {
            return authenticateRequest;
        }

        if (authenticateRequest == null || authenticateRequest.getToken() == null) {
            throw new BiqaAuthenticationLocalizedException("auth.exception.empty_password");
        }

        UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(authenticateRequest.getToken());
        authenticateRequest.setPassword(userNameWithPassword.password);
        authenticateRequest.setUsername(userNameWithPassword.username);
        return authenticateRequest;
    }

    public Mono<AuthenticateResultDto> authenticateRequest(@NotNull AuthenticateRequest authRequest) {
        return Mono.create(result -> {
            AuthenticateRequest authenticateRequest = tryAuthenticateResponseAsToken(authRequest);
            authFailedLimit.checkAuthFailedLimit(authenticateRequest);

            Iterator<AuthChainFilter> iterator = authChainFilters.iterator();
            final boolean[] done = {false};

            Function<AuthChainFilter, Mono<AuthChainOneFilterResult>> funcEmpToString = (AuthChainFilter next) ->
                    Mono.create(m -> next.process(authenticateRequest)
                            .doOnError(e -> result.error(e))
                            .subscribe(process -> {
                                if (process.isForceReturn()) {
                                    result.success(process.getAuthenticateResult());
                                    done[0] = true;
                                }

                                if (process.isSuccessProcessed()) {
                                    result.success(process.getAuthenticateResult());
                                    done[0] = true;
                                }

                                m.success(process);
                            }));

            Function<Function, Void> doExecuteNext = (a) -> {
                final AuthChainFilter[] next = {iterator.next()};

                funcEmpToString.apply(next[0]).subscribe(m -> {
                    if (!done[0]) {
                        if (iterator.hasNext()) {
                            next[0] = iterator.next();
                            a.apply(a);
                        } else {
                            result.success();
                        }
                    }
                });
                return null;
            };
            doExecuteNext.apply(doExecuteNext);
        });
    }

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        return null;
    }

    //  Authenticate incoming user
    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {

        List<String> authorization = serverWebExchange.getRequest().getHeaders().get("Authorization");
        if (authorization != null && authorization.size() == 1) {
            AuthenticateRequest authenticateRequest = new AuthenticateRequest();
            authenticateRequest.setToken(authorization.get(0));
            return this.authenticateRequest(authenticateRequest).flatMap(authenticateResult -> {
                AuthServerInternalAuth authentication = new AuthServerInternalAuth(authenticateResult);
                return Mono.just(new SecurityContextImpl(authentication));
            });
        }
        return Mono.empty();
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication);
    }
}
