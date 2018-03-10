/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "auth")
    @PostMapping
    public AuthenticateResult authenticateRequest(@RequestBody AuthenticateRequest authenticateRequest) {
        return requestAuthenticateService.authenticateRequest(authenticateRequest);
    }

}