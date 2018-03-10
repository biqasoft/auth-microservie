package com.biqasoft.users.oauth2.dto;

import com.biqasoft.users.useraccount.UserAccount;

import java.util.Date;
import java.util.List;

public class OAuth2MicroserviceNewCredentialsRequest {

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
