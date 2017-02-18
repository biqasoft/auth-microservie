/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.oauth2;

import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.common.dto.OAuth2NewTokenRequest;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.useraccount.UserAccount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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

    @Autowired
    public UserOAuth2Controller(OAuth2Repository oAuth2Repository) {
        this.oAuth2Repository = oAuth2Repository;
    }

    @ApiOperation(value = "add new user")
    @RequestMapping(value = "additional_username_password", method = RequestMethod.POST)
    public UserNameWithPassword create(@RequestBody OAuth2MicroserviceNewCredentialsRequest request){
        return oAuth2Repository.createAdditionalUsernameAndPasswordCredentialsOauth(request.getUserAccount(), request.getRolesRequested(), request.getExpireDate());
    }

    @ApiOperation(value = "add new user")
    @RequestMapping(value = "my_tokens")
    public List<UserAccountOAuth2> getMyTokens(){
        return oAuth2Repository.getCurrentUserTokens();
    }

    @ApiOperation(value = "add new user")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public UserAccountOAuth2 create(@RequestBody OAuth2MicroserviceNewTokenRequest request){
        return oAuth2Repository.createNewOAuthToken(request.getUserAccount(), request.getoAuth2Application(), request.getRequest());
    }

    @ApiOperation(value = "")
    @RequestMapping(value = "token/user_id/{userId}/token_id", method = RequestMethod.POST)
    public void deleteOauthTokenFromUserAccountById(@RequestBody DeleteToken deleteToken,  @PathVariable("userId") String userId) {
        oAuth2Repository.deleteOauthTokenFromUserAccountById(userId, deleteToken.getOauthToken());
    }

}

class DeleteToken{

    String oauthToken;

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }
}

class OAuth2MicroserviceNewTokenRequest {

    private UserAccount userAccount;
    private OAuth2Application oAuth2Application;
    private OAuth2NewTokenRequest request;

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public OAuth2Application getoAuth2Application() {
        return oAuth2Application;
    }

    public void setoAuth2Application(OAuth2Application oAuth2Application) {
        this.oAuth2Application = oAuth2Application;
    }

    public OAuth2NewTokenRequest getRequest() {
        return request;
    }

    public void setRequest(OAuth2NewTokenRequest request) {
        this.request = request;
    }
}

class OAuth2MicroserviceNewCredentialsRequest {

    private UserAccount userAccount;
    private List<String> rolesRequested;
    private Date expireDate;

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public List<String> getRolesRequested() {
        return rolesRequested;
    }

    public void setRolesRequested(List<String> rolesRequested) {
        this.rolesRequested = rolesRequested;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }
}
