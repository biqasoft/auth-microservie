/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.chain.UserAuthChecks;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResult;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.AuthServerInternalAuth;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.domain.DomainRepository;
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
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
@Service
public class RequestAuthenticateService implements ServerSecurityContextRepository, ReactiveAuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(RequestAuthenticateService.class);
    private final DomainRepository domainRepository;

    private final AuthFailedLimit authFailedLimit;

    private final UserAuthChecks userAuthChecks;

    private List<AuthChainFilter> authChainFilters;

    @Autowired
    public RequestAuthenticateService(DomainRepository domainRepository, AuthFailedLimit authFailedLimit, UserAuthChecks userAuthChecks, List<AuthChainFilter> authChainFilters) {
        this.domainRepository = domainRepository;
        this.authFailedLimit = authFailedLimit;
        this.userAuthChecks = userAuthChecks;

        if (CollectionUtils.isEmpty(authChainFilters)){
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

    public AuthenticateResult authenticateRequest(@NotNull AuthenticateRequest authenticateRequest) {
        authenticateRequest = tryAuthenticateResponseAsToken(authenticateRequest);
        authFailedLimit.checkAuthFailedLimit(authenticateRequest);

        for (AuthChainFilter authChainFilter : authChainFilters) {
            AuthChainOneFilterResult process = authChainFilter.process(authenticateRequest);
            if (process == null) {
                continue;
            }
            if (process.isForceReturn()){
                return process.getAuthenticateResult();
            }

            if (process.isSuccessProcessed()){
                return process.getAuthenticateResult();
            }

            // process by next filter
            continue;
        }

        // todo
        return null;

//         TODO:

//         else {



//            if (!user.getRoles().isEmpty()) {
//                auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRolesCSV());
//            } else {
//                auths = AuthorityUtils.NO_AUTHORITIES;
//            }

            // if we auth with root password - add special role
//        }


//
//        return response;
    }

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        return null;
    }

    //  Authenticate incoming user
    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {

        List<String> authorization = serverWebExchange.getRequest().getHeaders().get("Authorization");
        if (authorization != null && authorization.size() == 1)  {
            AuthenticateRequest authenticateRequest = new AuthenticateRequest();
            authenticateRequest.setToken(authorization.get(0));
            AuthenticateResult authenticateResult = this.authenticateRequest(authenticateRequest);
            if (authenticateResult == null) {
                return Mono.empty();
            }

            AuthServerInternalAuth authentication = new AuthServerInternalAuth(authenticateResult);
            return Mono.just(new SecurityContextImpl(authentication));
        }
        return Mono.empty();
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication);
    }
}
