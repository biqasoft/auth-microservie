/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.passwordcontrol;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.passwordcontrol.dto.PasswordResetDTO;
import com.biqasoft.users.passwordcontrol.dto.ResetPasswordTokenDTO;
import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("Auth controller")
@RequestMapping("/v1/users/auth/password")
public class UserPasswordController {

    private final PasswordResetRepository passwordResetRepository;

    @Autowired
    public UserPasswordController(PasswordResetRepository passwordResetRepository) {
        this.passwordResetRepository = passwordResetRepository;
    }

    @ApiOperation(value = "change password password for some user")
    @RequestMapping(value = "domain/change_password", method = RequestMethod.PUT)
    public PasswordResetDTO changePassword(@RequestBody UserAccountDbo userPosted, Principal principal) {
        return passwordResetRepository.unsafeResetPassword(userPosted, AuthHelper.castFromPrincipal(principal));
    }

    @ApiOperation("Reset user password by secret token")
    @RequestMapping(value = "domain/reset_password_by_token", method = RequestMethod.POST)
    public void removeAndResetPasswordTokenDao(@RequestBody ResetPasswordTokenDTO resetPasswordTokenDTO) throws Exception {
        boolean isValidToken = passwordResetRepository.validateResetPasswordToken(resetPasswordTokenDTO);

        if (!isValidToken) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("auth.reset.invalid.token");
        }

        passwordResetRepository.resetUserPasswordBySecretToken(resetPasswordTokenDTO);
    }

    @ApiOperation("Reset user password by secret token; user send login and auth microservice will create token and sent to user")
    @RequestMapping(value = "reset/create_token", method = RequestMethod.POST)
    public void createResetToken(@RequestBody ResetPasswordTokenDTO resetPasswordTokenDao) throws Exception {
        passwordResetRepository.addPasswordTokenDao(resetPasswordTokenDao);
    }

}