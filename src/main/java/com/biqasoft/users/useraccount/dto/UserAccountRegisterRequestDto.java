package com.biqasoft.users.useraccount.dto;

import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Nikita on 12.08.2016.
 */
@Data
public class UserAccountRegisterRequestDto {

    @NotNull
    @ApiModelProperty(value = "Just user account class", required = true)
    private UserAccountDbo userAccount;

    @ApiModelProperty("Does send email to new account with login and plain-text password")
    private boolean sendWelcomeEmail = false;

    @ApiModelProperty(value = "optional")
    private String domain;

    @ApiModelProperty(value = "Password for new user")
    private String password = null;

}
