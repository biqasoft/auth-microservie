package com.biqasoft.users.useraccount.dto;

import com.biqasoft.users.useraccount.UserAccount;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Nikita on 12.08.2016.
 */
 public class UserAccountAddRequestDTO{

    @ApiModelProperty("Just user account class")
    private UserAccount userAccount;

    @ApiModelProperty("Does send email to new account with login and plain-text password")
    private boolean sendWelcomeEmail = false;


    @ApiModelProperty("optional")
    private String domain;

    private String password = null;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public boolean isSendWelcomeEmail() {
        return sendWelcomeEmail;
    }

    public void setSendWelcomeEmail(boolean sendWelcomeEmail) {
        this.sendWelcomeEmail = sendWelcomeEmail;
    }
}
