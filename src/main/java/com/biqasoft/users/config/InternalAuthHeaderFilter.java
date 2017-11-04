/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.config;

import com.biqasoft.users.auth.CurrentUserContextProvider;
import com.biqasoft.users.authenticate.RequestAuthenticateService;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Authenticate incoming user
 * NOTE: that method does not authenticate user for auth endpoint,
 * only for auth microservice REST methods
 *
 * @author Nikita Bakaev
 *
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class InternalAuthHeaderFilter implements Filter {

    @Autowired
    private RequestAuthenticateService requestAuthenticateService;

    @Autowired
    private CurrentUserContextProvider currentUserContextProvider;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String authHeader = ((HttpServletRequest) req).getHeader("Authorization");

        // if no auth header or ! skip auth if request is to auth
        // TODO: refactor endpoint
        if (!StringUtils.isEmpty(authHeader) && !((HttpServletRequest) req).getServletPath().equals("/v1/users/auth")) {
            AuthenticateRequest authenticateRequest = new AuthenticateRequest();
            authenticateRequest.setToken(authHeader);

            AuthenticateResult authenticateResult = requestAuthenticateService.authenticateRequest(authenticateRequest);
            if (authenticateResult.getAuthenticated()) {
                AuthServerInternalAuth authentication = new AuthServerInternalAuth(authenticateResult);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                currentUserContextProvider.setDomain(authenticateResult.getDomain());
            }
        }

        chain.doFilter(req, res);
    }

    public void destroy() {
    }

    class AuthServerInternalAuth implements Authentication {

        private AuthenticateResult authenticateResult;

        public AuthServerInternalAuth(AuthenticateResult authenticateResult) {
            this.authenticateResult = authenticateResult;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authenticateResult.getAuths().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }

        @Override
        public Object getCredentials() {
            return " ";
        }

        @Override
        public Object getDetails() {
            return authenticateResult.getUserAccount();
        }

        @Override
        public Object getPrincipal() {
            User user = new User(authenticateResult.getUserAccount().getUsername(), (String) getCredentials(), authenticateResult.getUserAccount().getEnabled(),
                                 true, true, true, getAuthorities());
            return user;
        }

        @Override
        public boolean isAuthenticated() {
            return authenticateResult.getAuthenticated();
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return authenticateResult.getUserAccount().getUsername();
        }
    }

}