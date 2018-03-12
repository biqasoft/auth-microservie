/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate.dto;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.users.useraccount.UserAccount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: separate to DTO
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public class AuthenticateResult implements Serializable {

    // TODO: separate to DTO
    private UserAccount userAccount;

    // is user authenticated
    private Boolean authenticated = false;

    // list of user ROLES
    private List<String> auths = new ArrayList<>();

    // current authenticated user domain
    private Domain domain;

    @Deprecated(forRemoval = true)
    // current authenticated user domain settings
    private DomainSettings domainSettings;


    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Deprecated(forRemoval = true)
    public DomainSettings getDomainSettings() {
        return domainSettings;
    }

    @Deprecated(forRemoval = true)
    public void setDomainSettings(DomainSettings domainSettings) {
        this.domainSettings = domainSettings;
    }

    public List<String> getAuths() {
        return auths;
    }

    public void setAuths(List<String> auths) {
        this.auths = auths;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }
}
