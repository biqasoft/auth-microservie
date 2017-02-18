/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.config;

import com.biqasoft.users.auth.CurrentUserContextProvider;
import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.authenticate.RequestAuthenticateService;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
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
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/25/2016
 *         All Rights Reserved
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
        if (!StringUtils.isEmpty(authHeader) && !((HttpServletRequest) req).getServletPath().equals("/v1/users/auth")) {
            UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(authHeader);
            AuthenticateResponse authenticateResponse = requestAuthenticateService.authenticateResponse(AuthenticateRequest.fromUserNameWithPassword(userNameWithPassword));
            if (authenticateResponse.getAuthenticated()) {
                AuthServerInternalAuth authentication = new AuthServerInternalAuth(authenticateResponse);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                currentUserContextProvider.setDomain(authenticateResponse.getDomain());
            }
        }

        chain.doFilter(req, res);
    }

    public void destroy() {
    }

    class AuthServerInternalAuth implements Authentication {

        private AuthenticateResponse authenticateResponse;

        public AuthServerInternalAuth(AuthenticateResponse authenticateResponse) {
            this.authenticateResponse = authenticateResponse;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authenticateResponse.getAuths().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }

        @Override
        public Object getCredentials() {
            return " ";
        }

        @Override
        public Object getDetails() {
            return authenticateResponse.getUserAccount();
        }

        @Override
        public Object getPrincipal() {
            User user = new User(authenticateResponse.getUserAccount().getUsername(), (String) getCredentials(), authenticateResponse.getUserAccount().getEnabled(),
                                 true, true, true, getAuthorities());
            return user;
        }

        @Override
        public boolean isAuthenticated() {
            return authenticateResponse.getAuthenticated();
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return authenticateResponse.getUserAccount().getUsername();
        }
    }

}