package com.biqasoft.users.auth;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.core.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
public class CurrentUserImpl implements CurrentUser {

    private final CurrentUserContextProvider currentUserContextProvider;

    @Autowired
    public CurrentUserImpl(CurrentUserContextProvider currentUserContextProvider) {
        this.currentUserContextProvider = currentUserContextProvider;
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE));
    }

    @Override
    public UserAccount getCurrentUser() {
        return currentUserContextProvider.getUserAccount();
    }

    @Override
    public DomainSettings getCurrentUserDomain() {
        return currentUserContextProvider.getDomainSettings();
    }

    @Override
    public Domain getDomain() {
        return currentUserContextProvider.getDomain();
    }

    @Override
    public String printWithDateFormat(Date date) {
        String formatDate = getCurrentUser().getPersonalSettings().getDateFormat();
        if (StringUtils.isEmpty(formatDate)) {
            formatDate = DEFAULT_DATE_FORMAT;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(formatDate);
        return formatter.format(date);
    }

    @Override
    public String getLanguage() {
        return getCurrentUser().getLanguage();
    }

    @Override
    public boolean haveRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority(role));
    }
}
