package com.biqasoft.users.authenticate.limit;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import org.springframework.stereotype.Service;

/**
 * Created by Nikita on 14.08.2016.
 */
@Service
public class NoopAuthFailedLimit implements AuthFailedLimit {

    @Override
    public void processFailedAuth(AuthenticateRequest authenticateRequest) {

    }

    @Override
    public void checkAuthFailedLimit(AuthenticateRequest authenticateRequest) {

    }
}
