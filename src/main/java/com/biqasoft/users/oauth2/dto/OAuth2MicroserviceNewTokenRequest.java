package com.biqasoft.users.oauth2.dto;

import com.biqasoft.microservice.common.dto.oauth2.OAuth2NewTokenRequest;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import lombok.Data;

@Data
public class OAuth2MicroserviceNewTokenRequest {

    private UserAccount userAccount;
    private OAuth2Application oAuth2Application;
    private OAuth2NewTokenRequest request;

}
