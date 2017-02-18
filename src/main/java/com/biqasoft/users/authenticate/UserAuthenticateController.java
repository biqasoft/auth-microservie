/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("Auth controller")
@RequestMapping("/v1/users/auth")
public class UserAuthenticateController {

    private final RequestAuthenticateService requestAuthenticateService;

    @Autowired
    public UserAuthenticateController(RequestAuthenticateService requestAuthenticateService) {
        this.requestAuthenticateService = requestAuthenticateService;
    }

    @ApiOperation(value = "auth")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public AuthenticateResponse authenticateRequest(@RequestBody AuthenticateRequest authenticateRequest, HttpServletResponse response) {
        return requestAuthenticateService.authenticateResponse(authenticateRequest);
    }

}