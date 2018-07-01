package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common check for authentication by different methods
 */
@Service
public class UserAuthChecks {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthChecks.class);

    private final Boolean enableRootSystemUser;
    private final String passwordRootSystemUser;
    private final AuthFailedLimit authFailedLimit;

    public UserAuthChecks(@Value("${biqa.security.global.root.enable:false}") boolean enableRootSystemUser,
                          @Value("${biqa.security.global.root.password:NO_PASSWORD}") String passwordRootSystemUser, AuthFailedLimit authFailedLimit) {
        this.enableRootSystemUser = enableRootSystemUser;
        this.passwordRootSystemUser = passwordRootSystemUser;
        this.authFailedLimit = authFailedLimit;
    }

    /**
     * 1) Check that user can authenticate with IP address
     * 2) Check that user domain is active
     * <p>
     * If IP address is not allowed, or domain is inactive - {@link com.biqasoft.users.config.BiqaAuthenticationLocalizedException} exception will be thrown
     *
     * @param remoteAddress IP address of remote user
     * @param userAccount   user
     * @param domain        user domain
     */
    public void checkUserIpPatternAndActiveDomain(String remoteAddress, UserAccount userAccount, Domain domain) {
        if (userAccount != null) {

            // check for allowed IP address for user regexp pattern
            if (!StringUtils.isEmpty(userAccount.getIpPattern())) {
                Pattern pattern = Pattern.compile(userAccount.getIpPattern());
                Matcher matcher = pattern.matcher(remoteAddress);

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

    public void checkUserEnabled(UserAccount user, AuthenticateRequest authenticateRequest) {
        if (!(UserAccount.UserAccountStatus.STATUS_APPROVED.name().equals(user.getStatus()))) {
            logger.info("Username {}: not approved status is {} | {}", user.getUsername(), user.getStatus(), UserAccount.UserAccountStatus.STATUS_APPROVED.name());
            this.authFailedLimit.processFailedAuth(authenticateRequest);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.not_approved");
        }
        if (!user.getEnabled()) {
            logger.info("Username {}: disabled", user.getUsername());
            this.authFailedLimit.processFailedAuth(authenticateRequest);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.user_disabled");
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
    public boolean checkRootAccount(String username, String password) {
        // to authenticated under all users
        if (enableRootSystemUser && passwordRootSystemUser.equals(password)) {
            logger.warn("biqa: SECURITY - USED GLOBAL ROOT PASSWORD TO AUTH Username {} ", username);
            return true;
        } else {
            logger.info("Username {} invalid password", username);
            return false;
        }
    }

}
