/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.auth;

import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.useraccount.dbo.UserAccount;

import java.util.Date;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/17/2016.
 * All Rights Reserved
 */
public interface CurrentUserContextProvider {

    UserAccount getUserAccount();

    Domain getDomain();

    void setUserAccount(UserAccount userAccount);

    void setDomain(Domain domain);

    String printWithDateFormat(Date date);

    String getLanguage();

    boolean haveRole(String role);
}
