package com.biqasoft.users.authenticate.chain.impl;

import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// TODO: implement LDAP/AD
@Service
public class LdapAuthFilter implements AuthChainFilter {

    @Override
    public Mono<AuthChainOneFilterResult> process(AuthenticateRequest authenticateRequest) {
        return Mono.just(EMPTY_RESULT);
    }

    @Override
    public String getName() {
        return "LDAP/AD";
    }

    @Override
    public String getDescription() {
        return "Authentication via LDAP and Active Directory";
    }

    @Override
    public boolean is2FASupported() {
        return false;
    }

}
