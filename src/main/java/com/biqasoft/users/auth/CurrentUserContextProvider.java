/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.auth;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.core.useraccount.UserAccount;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/17/2016.
 * All Rights Reserved
 */
public interface CurrentUserContextProvider {
    UserAccount getUserAccount();

    DomainSettings getDomainSettings();

    Domain getDomain();

    void setUserAccount(UserAccount userAccount);
    void setDomain(Domain domain);
    void setDomainSettings(DomainSettings domainSettings);
}
