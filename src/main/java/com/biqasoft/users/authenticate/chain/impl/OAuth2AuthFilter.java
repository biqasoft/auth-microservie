package com.biqasoft.users.authenticate.chain.impl;

import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import com.biqasoft.users.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Authentication via OAuth2 token
 */
@Service
public class OAuth2AuthFilter implements AuthChainFilter {

    private final OAuth2Repository oAuth2Repository;
    private final AuthFailedLimit authFailedLimit;

    @Autowired
    public OAuth2AuthFilter(OAuth2Repository oAuth2Repository, AuthFailedLimit authFailedLimit) {
        this.oAuth2Repository = oAuth2Repository;
        this.authFailedLimit = authFailedLimit;
    }

    @Override
    public AuthChainOneFilterResult process(AuthenticateRequest authenticateRequest) {

        AuthChainOneFilterResult result = new AuthChainOneFilterResult();
        UserAccount user;

        String password = authenticateRequest.getPassword();
        String username = authenticateRequest.getUsername();

        List<String> auths;

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
                auths = token.getRoles();

                // special OAuth role that grant all actual
                // user permissions(Spring roles)
                if (token.getRoles().contains(SYSTEM_ROLES.OAUTH_ALL_USER)) {
                    auths = user.getRoles();
                }
            } else {
                auths = new ArrayList<>();
            }
            auths.add(SYSTEM_ROLES.OAUTH_AUTHENTICATED);

            result.getAuthenticateResult().setUserAccount(user);
            result.getAuthenticateResult().setAuths(auths);
            result.setSuccessProcessed(true);
        }

        return result;
    }

    @Override
    public String getName() {
        return "OAuth2Auth";
    }

    @Override
    public String getDescription() {
        return "Authentication via OAuth2 application token";
    }

    @Override
    public boolean is2FASupported() {
        return false;
    }

}
