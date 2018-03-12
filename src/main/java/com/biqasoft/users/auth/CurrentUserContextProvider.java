/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.auth;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;

import java.util.Date;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/17/2016.
 * All Rights Reserved
 */
public interface CurrentUserContextProvider {

    com.biqasoft.users.useraccount.UserAccount getUserAccount();

    @Deprecated(forRemoval = true)
    com.biqasoft.users.useraccount.UserAccount getCurrentUser();

    DomainSettings getDomainSettings();

    Domain getDomain();

    void setUserAccount(com.biqasoft.users.useraccount.UserAccount userAccount);

    void setDomain(Domain domain);

    void setDomainSettings(DomainSettings domainSettings);

    String printWithDateFormat(Date date);

    String getLanguage();

    boolean haveRole(String role);
}
