package com.biqasoft.users.authenticate.chain;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.useraccount.UserAccount;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common check for authentication by different methods
 */
@Service
public class UserAuthChecks {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthChecks.class);

    /**
     * This function check if 2FA token is valid for provided user
     * @param userAccount user
     * @param code2FA provided by user token
     * @return true if 2FA code is valid. false if token is wrong
     */
    public boolean isTwoStepCodeValidForUser(UserAccount userAccount, String code2FA) {
        String currentValidCode;
        try {
            currentValidCode = TimeBasedOneTimePasswordUtil.generateCurrentNumber(userAccount.getTwoStepCode());
        } catch (GeneralSecurityException e) {
            logger.error("Error creating 2 step auth code", e);
            return false;
        }
        return currentValidCode.equals(code2FA);
    }

    /**
     * 1) Check that user can authenticate with IP address
     * 2) Check that user domain is active
     *
     * If IP address is not allowed, or domain is inactive - {@link com.biqasoft.users.config.BiqaAuthenticationLocalizedException} exception will be thrown
     *
     * @param remoteAddress IP address of remote user
     * @param userAccount user
     * @param domain user domain
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
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.invalid_password");
        }
        throw new BadCredentialsException("");
    }

}
