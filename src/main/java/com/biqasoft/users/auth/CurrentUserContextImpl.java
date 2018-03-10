package com.biqasoft.users.auth;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.core.useraccount.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserContextImpl implements CurrentUserContextProvider {

    @Override
    public UserAccount getUserAccount() {
        return null;
    }

    @Override
    public DomainSettings getDomainSettings() {
        return null;
    }

    @Override
    public Domain getDomain() {
        return null;
    }

    @Override
    public void setUserAccount(UserAccount userAccount) {

    }

    @Override
    public void setDomain(Domain domain) {

    }

    @Override
    public void setDomainSettings(DomainSettings domainSettings) {

    }
}
