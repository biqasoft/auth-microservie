package com.biqasoft.users.auth;

import com.biqasoft.common.exceptions.InvalidStateException;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.domain.settings.DomainSettingsRepository;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Optimised version of class {@link com.biqasoft.auth.CurrentUserContextProviderImpl} which use local repositories
 * instead of http request to themselves microservice
 *
 * Data per request holder
 *
 * we do not need synchronization on getUserAccount etc because this is context per http request/thread
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Primary
public class CurrentUserContextProviderImpl implements CurrentUserContextProvider {

    private UserAccount userAccount = null;
    private DomainSettings domainSettings = null;
    private Domain domain = null;

    private final DomainSettingsRepository domainSettingsRepository;
    private final UserAccountRepository userAccountRepository;
    private final DomainRepository domainRepository;

    @Autowired
    public CurrentUserContextProviderImpl(DomainSettingsRepository domainSettingsRepository, UserAccountRepository userAccountRepository, DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
        this.domainSettingsRepository = domainSettingsRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserAccount getUserAccount() {
        if (userAccount == null) {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a == null) {
                return null;
            }

            User currentUser;
            try {
                // allow to pre populate scope to avoid http request
                if (a.getDetails() != null && (a.getDetails() instanceof UserAccount)) {
                    userAccount = (UserAccount) a.getDetails();
                    return userAccount;
                }

                currentUser = (User) a.getPrincipal();
            } catch (Exception e) {
                return null;
            }

            com.biqasoft.users.useraccount.UserAccount internalUserEntity = userAccountRepository.findByUsernameOrOAuthToken(currentUser.getUsername());
            String domain = getDomainForInternalUser(internalUserEntity);

            if (internalUserEntity == null || StringUtils.isEmpty(domain)) {
                throw new InvalidStateException("NULL domain for user " + userAccount.getId());
            }

            userAccount = TransformUserAccountEntity.transform(internalUserEntity);
            setDomain(domainRepository.findDomainById(domain));
        }

        return userAccount;
    }

    // this is main method when we decide to which domain user belong
    public static String getDomainForInternalUser(com.biqasoft.users.useraccount.UserAccount userAccount){
        return userAccount.getDomain(); // just store domain as string
    }

    @Override
    public DomainSettings getDomainSettings() {
        if (domainSettings == null) {
            domainSettings = domainSettingsRepository.findDomainSettingsById(getDomain().getDomain());
        }

        return domainSettings;
    }

    public Domain getDomain() {
        if (domain == null) {
            getUserAccount(); // set domain

            // if still error
            if (domain == null){
                throw new InvalidStateException("Null domain");
            }
        }

        return domain;
    }

    @Override
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @Override
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public void setDomainSettings(DomainSettings domainSettings) {
        this.domainSettings = domainSettings;
    }

}
