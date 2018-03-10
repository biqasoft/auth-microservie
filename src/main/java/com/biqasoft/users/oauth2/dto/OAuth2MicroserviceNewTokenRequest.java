package com.biqasoft.users.oauth2.dto;

import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.common.dto.OAuth2NewTokenRequest;
import com.biqasoft.users.useraccount.UserAccount;

public class OAuth2MicroserviceNewTokenRequest {

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
