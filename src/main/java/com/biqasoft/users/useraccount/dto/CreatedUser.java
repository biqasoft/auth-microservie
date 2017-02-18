/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.useraccount.dto;


import com.biqasoft.users.useraccount.UserAccount;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 8/7/2016
 *         All Rights Reserved
 */
public class CreatedUser {

    private UserAccount userAccount;

    // do not add @JsonIgnore
    private String password;
    private String domain;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
