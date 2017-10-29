/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.users.auth.CurrentUserContextProviderImpl;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.chain.UserAuthChecks;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.domain.settings.DomainSettingsRepository;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
@Service
public class RequestAuthenticateService {

    private final OAuth2Repository oAuth2Repository;
    private static final Logger logger = LoggerFactory.getLogger(RequestAuthenticateService.class);
    private final UserAccountRepository userAccountRepository;

    private final DomainSettingsRepository domainSettingsRepository;
    private final DomainRepository domainRepository;

    private final SimpleGrantedAuthority rootAuthority = new SimpleGrantedAuthority(SYSTEM_ROLES.ROOT_USER);

    private final Boolean enableRootSystemUser;
    private final String passwordRootSystemUser;

    private final AuthFailedLimit authFailedLimit;

    private final UserAuthChecks userAuthChecks;

    private List<AuthChainFilter> authChainFilters;

    @Autowired
    public RequestAuthenticateService(OAuth2Repository oAuth2Repository, UserAccountRepository userAccountRepository,
                                      @Value("${biqa.security.global.root.enable:false}") Boolean enableRootSystemUser,
                                      @Value("${biqa.security.global.root.password:NO_PASSWORD}") String passwordRootSystemUser, PasswordEncoder encoder,
                                      DomainRepository domainRepository, DomainSettingsRepository domainSettingsRepository, AuthFailedLimit authFailedLimit,
                                      UserAuthChecks userAuthChecks, List<AuthChainFilter> authChainFilters) {
        this.oAuth2Repository = oAuth2Repository;
        this.userAccountRepository = userAccountRepository;
        this.enableRootSystemUser = enableRootSystemUser;
        this.passwordRootSystemUser = passwordRootSystemUser;
        this.domainRepository = domainRepository;
        this.domainSettingsRepository = domainSettingsRepository;
        this.authFailedLimit = authFailedLimit;
        this.userAuthChecks = userAuthChecks;

        if (CollectionUtils.isEmpty(authChainFilters)){
            throw new IllegalArgumentException("AuthChainFilter can not be empty");
        }
        this.authChainFilters = authChainFilters;
    }

    private AuthenticateRequest tryAuthenticateResponseAsToken(@NotNull AuthenticateRequest authenticateRequest) {

        // if we already have username and password - skip processing as token
        if (authenticateRequest != null && !StringUtils.isEmpty(authenticateRequest.getUsername()) && !StringUtils.isEmpty(authenticateRequest.getPassword())) {
            return authenticateRequest;
        }

        if (authenticateRequest == null || authenticateRequest.getToken() == null) {
            throw new BiqaAuthenticationLocalizedException("auth.exception.empty_password");
        }

        UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(authenticateRequest.getToken());
        authenticateRequest.setPassword(userNameWithPassword.password);
        authenticateRequest.setUsername(userNameWithPassword.username);
        return authenticateRequest;
    }

    public AuthenticateResponse authenticateResponse(@NotNull AuthenticateRequest authenticateRequest) {
        authenticateRequest = tryAuthenticateResponseAsToken(authenticateRequest);
        authFailedLimit.checkAuthFailedLimit(authenticateRequest);

        boolean isRootUser = false;

        List<GrantedAuthority> auths;

        for (AuthChainFilter authChainFilter : authChainFilters) {
            AuthChainOneFilterResult process = authChainFilter.process(authenticateRequest);
            if (process.isForceReturn()){
                return process.getAuthenticateResponse();
            }

            if (process.isSuccessProcessed()){
                return process.getAuthenticateResponse();
            }

            // process by next filter
            continue;
        }

        if (!StringUtils.hasText(password)) {
            logger.info("Username {}: no password provided", username);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.empty_password");
        }

         else {


            if (!(UserAccount.UserAccountStatus.STATUS_APPROVED.name().equals(user.getStatus()))) {
                logger.info("Username {}: not approved status is {} | {}", username, user.getStatus(), UserAccount.UserAccountStatus.STATUS_APPROVED.name());
                authFailedLimit.processFailedAuth(authenticateRequest);
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.not_approved");
            }
            if (!user.getEnabled()) {
                logger.info("Username {}: disabled", username);
                authFailedLimit.processFailedAuth(authenticateRequest);
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.user_disabled");
            }
            if (!user.getRoles().isEmpty()) {
                auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRolesCSV());
            } else {
                auths = AuthorityUtils.NO_AUTHORITIES;
            }

            // if we auth with root password - add special role
            if (isRootUser) auths.add(rootAuthority);
        }

        // security check than user can not add ROLE_ROOT user manually
        if (!isRootUser) {
            if (auths.contains(rootAuthority)) {
                auths.remove(rootAuthority);
            }
        }

//        AuthenticateResponse response = new AuthenticateResponse();
//        response.setAuths(auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
//        response.setUserAccount(UserAccountMapper.transform(user));
//        response.setAuthenticated(true);

        response.setDomain(domainRepository.findDomainById(CurrentUserContextProviderImpl.getDomainForInternalUser(user)));
//        response.setDomainSettings(domainSettingsRepository.findDomainSettingsById(user.getDomain())); // do not add domain settings because they not always need

        if (!StringUtils.isEmpty(authenticateRequest.getIp())){
            userAuthChecks.checkUserIpPatternAndActiveDomain(authenticateRequest.getIp(), user, response.getDomain());
        }

        return response;
    }

}
