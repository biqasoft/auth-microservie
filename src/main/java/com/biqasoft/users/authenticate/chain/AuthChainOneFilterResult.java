package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateResultDto;
import lombok.Data;

/**
 * Data object for authenticate result by {@link AuthChainFilter}
 */
@Data
public class AuthChainOneFilterResult {

    // true - if processing must be interrupted.
    // And no any other filters will be applied
    private boolean forceReturn;

    // true - if request if successfully processed by that filter.
    // And no any other filters will be applied
    private boolean isSuccessProcessed;

    // result of processing by filter
    private AuthenticateResultDto authenticateResult = new AuthenticateResultDto();

}
