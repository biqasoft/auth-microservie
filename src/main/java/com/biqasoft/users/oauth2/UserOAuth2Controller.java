/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.oauth2;

import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.oauth2.dto.DeleteToken;
import com.biqasoft.users.oauth2.dto.OAuth2MicroserviceNewCredentialsRequest;
import com.biqasoft.users.oauth2.dto.OAuth2MicroserviceNewTokenRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceUsersRepository}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("User accounts management in domain")
@RequestMapping("/v1/users/oauth2")
public class UserOAuth2Controller {

    private final OAuth2Repository oAuth2Repository;

    public UserOAuth2Controller(OAuth2Repository oAuth2Repository) {
        this.oAuth2Repository = oAuth2Repository;
    }

    @ApiOperation(value = "add new user")
    @PostMapping(value = "additional_username_password")
    public Mono<UserNameWithPassword> create(@RequestBody OAuth2MicroserviceNewCredentialsRequest request){
        return oAuth2Repository.createAdditionalUsernameAndPasswordCredentialsOauth(request.getUserAccount(), request.getRolesRequested(), request.getExpireDate());
    }

    @ApiOperation(value = "add new user")
    @GetMapping(value = "my_tokens")
    public Flux<UserAccountOAuth2> getMyTokens(){
        return oAuth2Repository.getCurrentUserTokens();
    }

    @ApiOperation(value = "add new user")
    @PostMapping(value = "")
    public Mono<UserAccountOAuth2> create(@RequestBody OAuth2MicroserviceNewTokenRequest request){
        return oAuth2Repository.createNewOAuthToken(request.getUserAccount(), request.getoAuth2Application(), request.getRequest());
    }

    @ApiOperation(value = "")
    @PostMapping(value = "token/user_id/{userId}/token_id")
    public Mono<Void> deleteOauthTokenFromUserAccountById(@RequestBody DeleteToken deleteToken, @PathVariable("userId") String userId) {
        return oAuth2Repository.deleteOauthTokenFromUserAccountById(userId, deleteToken.getOauthToken());
    }

}

