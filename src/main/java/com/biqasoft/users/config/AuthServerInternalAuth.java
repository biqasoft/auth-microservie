package com.biqasoft.users.config;

import com.biqasoft.users.authenticate.dto.AuthenticateResultDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.stream.Collectors;

public class AuthServerInternalAuth implements Authentication {

    private final AuthenticateResultDto authenticateResult;

    public AuthServerInternalAuth(AuthenticateResultDto authenticateResult) {
        this.authenticateResult = authenticateResult;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authenticateResult.getAuths().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        throw new IllegalStateException("Not supported operation getCredentials()");
    }

    @Override
    public Object getDetails() {
        return authenticateResult.getUserAccount();
    }

    @Override
    public Object getPrincipal() {
        return new User(authenticateResult.getUserAccount().getUsername(), (String) getCredentials(), authenticateResult.getUserAccount().getEnabled(),
                             true, true, true, getAuthorities());
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

    public AuthenticateResultDto getAuthenticateResult() {
        return authenticateResult;
    }
}
