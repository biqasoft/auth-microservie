package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import reactor.core.publisher.Mono;

/**
 * Interface for different authentication type: e.g. username+password, OAuth2 token, LDAP/AD.
 * All instances of that interface are injected by {@link com.biqasoft.users.authenticate.RequestAuthenticateService}
 */
public interface AuthChainFilter {

    AuthChainOneFilterResult EMPTY_RESULT = new AuthChainOneFilterResult();

    /**
     * Process authentication by filter
     * @param authenticateRequest auth request
     * @return result of processing request by filter
     */
    Mono<AuthChainOneFilterResult> process(AuthenticateRequest authenticateRequest);

    /**
     * @return Name of auth type
     */
    String getName();

    /**
     * @return Human readable description
     */
    String getDescription();

    /**
     *
     * @return is 2FA is supported by current filter
     */
    boolean is2FASupported();

}

