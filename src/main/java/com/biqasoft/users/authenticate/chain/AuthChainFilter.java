package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;

/**
 * Interface for different authentication type: e.g. username+password, OAuth2 token, LDAP/AD
 */
public interface AuthChainFilter {

    /**
     * Process authentication by filter
     * @param authenticateRequest auth request
     * @return result of processing request by filter
     */
    AuthChainOneFilterResult process(AuthenticateRequest authenticateRequest);

    /**
     * @return Name of auth type
     */
    String getName();

    /**
     *
     * @return is 2FA is supported by current filter
     */
    boolean twoFactorSupported();

}

