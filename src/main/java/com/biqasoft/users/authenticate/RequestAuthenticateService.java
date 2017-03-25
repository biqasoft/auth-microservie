/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.users.auth.CurrentUserContextProviderImpl;
import com.biqasoft.users.auth.TransformUserAccountEntity;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.domain.settings.DomainSettingsRepository;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final PasswordEncoder encoder;

    private final DomainSettingsRepository domainSettingsRepository;
    private final DomainRepository domainRepository;

    private final SimpleGrantedAuthority rootAuthority = new SimpleGrantedAuthority(SYSTEM_ROLES.ROOT_USER);

    private final Boolean enableRootSystemUser;
    private final String passwordRootSystemUser;

    private final AuthFailedLimit authFailedLimit;

    @Autowired
    public RequestAuthenticateService(OAuth2Repository oAuth2Repository, UserAccountRepository userAccountRepository,
                                      @Value("${biqa.security.global.root.enable:false}") Boolean enableRootSystemUser,
                                      @Value("${biqa.security.global.root.password:NO_PASSWORD}") String passwordRootSystemUser, PasswordEncoder encoder,
                                      DomainRepository domainRepository, DomainSettingsRepository domainSettingsRepository, AuthFailedLimit authFailedLimit) {
        this.oAuth2Repository = oAuth2Repository;
        this.userAccountRepository = userAccountRepository;
        this.enableRootSystemUser = enableRootSystemUser;
        this.passwordRootSystemUser = passwordRootSystemUser;
        this.encoder = encoder;
        this.domainRepository = domainRepository;
        this.domainSettingsRepository = domainSettingsRepository;
        this.authFailedLimit = authFailedLimit;
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

        UserAccount user;
        boolean isRootUser = false;
        String password = authenticateRequest.getPassword();
        String username = authenticateRequest.getUsername();

        List<GrantedAuthority> auths;

        if (!StringUtils.hasText(password)) {
            logger.info("Username {}: no password provided", username);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.empty_password");
        }

        // if username start with following characters - this is oauth auto
        // generated username
        if (username.startsWith(SYSTEM_CONSTS.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            user = oAuth2Repository.findUserByOAuth2TokenIDAndUserNameToken(password, username);
            if (user == null) {
                authFailedLimit.processFailedAuth(authenticateRequest);
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.oauth2.invalid_token");
            }

            // we have token, no need to check NPE
            UserAccountOAuth2 token = user.getoAuth2s().stream().filter(x -> x.getUserName().equals(username)).findFirst().get();

            // check OAuth token for disable
            if (!token.isEnabled()) {
                authFailedLimit.processFailedAuth(authenticateRequest);
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.oauth2.token_disabled");
            }

            // check is token expired
            if (token.getExpire() != null) {
                if (new Date().after(token.getExpire())) {
                    authFailedLimit.processFailedAuth(authenticateRequest);
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.oauth2.token_expired");
                }
            }

            if (!token.getRoles().isEmpty()) {
                // this is roles, that user grant for this oauth token
                auths = AuthorityUtils.commaSeparatedStringToAuthorityList(token.getRolesCSV());

                // special OAuth role that grant all actual
                // user permissions(Spring roles)
                if (token.getRoles().contains(SYSTEM_ROLES.OAUTH_ALL_USER)) {
                    auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRolesCSV());
                }
            } else {
                auths = AuthorityUtils.NO_AUTHORITIES;
            }
            auths.add(new SimpleGrantedAuthority(SYSTEM_ROLES.OAUTH_AUTHENTICATED));

            // this is plain text password
        } else {
            user = userAccountRepository.findByUsernameOrOAuthToken(username);
            if (user == null) {
                logger.info("Username {}: user not found", username);
                authFailedLimit.processFailedAuth(authenticateRequest);
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.no_user");
            }

            // plain text password with active 2 step auth require send as token
            if (user.isTwoStepActivated()) {
                String token = authenticateRequest.getToken();
                if (StringUtils.isEmpty(token)) {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                }

                UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(token);
                if (userNameWithPassword != null && !StringUtils.isEmpty(userNameWithPassword.getTwoStepCode())) {

                    if (!isTwoStepCodeValidForUser(user, userNameWithPassword.twoStepCode)) {
                        ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                    }

                } else {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                }
            }

            // encoder.matches() is hash - long operation
            if (!encoder.matches(password, user.getPassword())) {
                isRootUser = checkRootAccount(username, password);
            }

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

        AuthenticateResponse response = new AuthenticateResponse();
        response.setAuths(auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        response.setUserAccount(TransformUserAccountEntity.transform(user));
        response.setAuthenticated(true);

        response.setDomain(domainRepository.findDomainById(CurrentUserContextProviderImpl.getDomainForInternalUser(user)));
//        response.setDomainSettings(domainSettingsRepository.findDomainSettingsById(user.getDomain())); // do not add domain settings because they not always need

        if (!StringUtils.isEmpty(authenticateRequest.getIp())){
            checkUserIpPattern(authenticateRequest.getIp(), user, response.getDomain());
        }

        return response;
    }

    public static boolean isTwoStepCodeValidForUser(UserAccount userAccount, String providedCode) {
        String currentValidCode;
        try {
            currentValidCode = TimeBasedOneTimePasswordUtil.generateCurrentNumber(userAccount.getTwoStepCode());
        } catch (GeneralSecurityException e) {
            logger.error("Error creating 2 step auth code", e);
            return false;
        }
        return currentValidCode.equals(providedCode);
    }

    private void checkUserIpPattern(String remoteAddr, UserAccount userAccount, Domain domain) {
        if (userAccount != null) {

            // check for allowed IP address for user regexp pattern
            if (!StringUtils.isEmpty(userAccount.getIpPattern())) {
                Pattern pattern = Pattern.compile(userAccount.getIpPattern());
                Matcher matcher = pattern.matcher(remoteAddr);

                if (!matcher.matches()) {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.ip_deny");
                }
            }

            // check for active domain
            if (!domain.isActive()) {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.domain_inactive");
            }
        }
    }

    /**
     * If requested user password do not equal to real user password
     * check if it is a root user password
     * otherwise - error in authentication
     *
     * @param username
     * @param password
     * @return
     */
    private boolean checkRootAccount(String username, String password) {
        // to authenticated under all users
        if (enableRootSystemUser && passwordRootSystemUser.equals(password)) {
            logger.warn("biqa: SECURITY - USED GLOBAL ROOT PASSWORD TO AUTH Username {} ", username);
            return true;
        } else {
            logger.info("Username {} invalid password", username);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.invalid_password");
        }
        throw new BadCredentialsException("");
    }

}
