package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;

public interface AuthChainFilter {

    AuthChainOneFilterResult process(AuthenticateRequest authenticateRequest);

}

