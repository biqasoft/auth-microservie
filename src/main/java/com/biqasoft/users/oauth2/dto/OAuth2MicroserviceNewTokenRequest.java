package com.biqasoft.users.oauth2.dto;

import com.biqasoft.microservice.common.dto.oauth2.OAuth2NewTokenRequest;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import lombok.Data;

@Data
public class OAuth2MicroserviceNewTokenRequest {

    private String userAccountId;
    private OAuth2Application oAuth2Application;
    private OAuth2NewTokenRequest request;

}
