package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import com.biqasoft.users.useraccount.UserAccount;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class AuthChainOneFilterResult {

    private boolean forceReturn;
    private boolean isSuccessProcessed;

    private AuthenticateResponse authenticateResponse = new AuthenticateResponse();

    public AuthenticateResponse getAuthenticateResponse() {
        return authenticateResponse;
    }

    public void setAuthenticateResponse(AuthenticateResponse authenticateResponse) {
        this.authenticateResponse = authenticateResponse;
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
