package com.biqasoft.users.auth;

import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.domain.DomainSettings;
import com.biqasoft.users.config.AuthServerInternalAuth;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentUserCtx implements CurrentUserContextProvider {

    private Domain domain;
    private DomainSettings domainSettings;
    private com.biqasoft.users.useraccount.UserAccount account;

    private static final String DEFAULT_LANGUAGE = "en_US";
    private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";

    public CurrentUserCtx(AuthServerInternalAuth auth) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE));

        domain = auth.getAuthenticateResult().getDomain();
        domainSettings = auth.getAuthenticateResult().getDomainSettings();
        account = auth.getAuthenticateResult().getUserAccount();
    }

    @Override
    public com.biqasoft.users.useraccount.UserAccount getUserAccount() {
        return account;
    }

    @Override
    public DomainSettings getDomainSettings() {
        return domainSettings;
    }

    @Override
    public Domain getDomain() {
        return domain;
    }

    @Override
    public void setUserAccount(com.biqasoft.users.useraccount.UserAccount userAccount) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setDomain(Domain domain) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setDomainSettings(DomainSettings domainSettings) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String printWithDateFormat(Date date) {
        String formatDate = getUserAccount().getPersonalSettings().getDateFormat();
        if (StringUtils.isEmpty(formatDate)) {
            formatDate = DEFAULT_DATE_FORMAT;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(formatDate);
        return formatter.format(date);
    }

    @Override
    public String getLanguage() {
        return getUserAccount().getLanguage();
    }

    @Override
    public boolean haveRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority(role));
    }
}
