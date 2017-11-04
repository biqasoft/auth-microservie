package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateResult;

/**
 * Data object for authenticate result by {@link AuthChainFilter}
 */
public class AuthChainOneFilterResult {

    // true - if processing must be interrupted.
    // And no any other filters will be applied
    private boolean forceReturn;

    // true - if request if successfully processed by that filter.
    // And no any other filters will be applied
    private boolean isSuccessProcessed;

    // result of processing by filter
    private AuthenticateResult authenticateResult = new AuthenticateResult();

    public AuthenticateResult getAuthenticateResult() {
        return authenticateResult;
    }

    public void setAuthenticateResult(AuthenticateResult authenticateResult) {
        this.authenticateResult = authenticateResult;
    }

    public boolean isSuccessProcessed() {
        return isSuccessProcessed;
    }

    public void setSuccessProcessed(boolean successProcessed) {
        isSuccessProcessed = successProcessed;
    }


    public boolean isForceReturn() {
        return forceReturn;
    }

    public void setForceReturn(boolean forceReturn) {
        this.forceReturn = forceReturn;
    }
}
