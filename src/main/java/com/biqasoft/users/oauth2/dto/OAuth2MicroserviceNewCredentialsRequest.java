package com.biqasoft.users.oauth2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OAuth2MicroserviceNewCredentialsRequest {

    @ApiModelProperty("Id of user")
    private String userAccountId;

    @ApiModelProperty("List of roles to grant to token. Empty if all roles")
    private List<String> rolesRequested;

    @ApiModelProperty("Expire date of token. Empty is infinite or until revoke")
    private Date expireDate;

}
