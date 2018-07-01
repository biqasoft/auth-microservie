package com.biqasoft.users.oauth2.dto;

import com.biqasoft.users.useraccount.dbo.UserAccount;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OAuth2MicroserviceNewCredentialsRequest {

    private UserAccount userAccount;
    private List<String> rolesRequested;
    private Date expireDate;

}
