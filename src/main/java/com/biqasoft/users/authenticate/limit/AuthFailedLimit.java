package com.biqasoft.users.authenticate.limit;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;

/**
 * Created by Nikita on 14.08.2016.
 */
public interface AuthFailedLimit {
    void processFailedAuth(AuthenticateRequest authenticateRequest);

    void checkAuthFailedLimit(AuthenticateRequest authenticateRequest);
}
