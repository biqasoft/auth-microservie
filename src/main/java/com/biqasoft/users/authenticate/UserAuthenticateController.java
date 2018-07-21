/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResultDto;
import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * @author Nikita Bakaev
 */
@RestController
@Api("Auth controller")
@RequestMapping("/v1/users/auth")
public class UserAuthenticateController {

    private final RequestAuthenticateService requestAuthenticateService;

    public UserAuthenticateController(RequestAuthenticateService requestAuthenticateService) {
        this.requestAuthenticateService = requestAuthenticateService;
    }

    @ApiOperation(value = "auth", notes = "Private API for other microservices")
    @PostMapping
    public Mono<AuthenticateResultDto> authenticateRequest(@RequestBody AuthenticateRequest authenticateRequest) {
        return requestAuthenticateService.authenticateRequest(authenticateRequest);
    }

    @ApiOperation(value = "auth", notes = "Public API - current authentication in auth microservice")
    @GetMapping
    public MeResponseDto authenticateRequestMe(Principal principal) {
        CurrentUserCtx currentUserCtx = AuthHelper.castFromPrincipal(principal);

        MeResponseDto me = new MeResponseDto();
        me.setAccount(currentUserCtx.getUserAccount());
        me.setDomain(currentUserCtx.getDomain());

        return me;
    }

    @Data
    static class MeResponseDto {
        private Domain domain;
        private UserAccountDbo account;
    }

}