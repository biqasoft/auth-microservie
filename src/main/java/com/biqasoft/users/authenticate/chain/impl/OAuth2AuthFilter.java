package com.biqasoft.users.authenticate.chain.impl;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.chain.UserAuthChecks;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.biqasoft.users.authenticate.AuthHelper.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX;

/**
 * Authentication via OAuth2 token
 */
@Service
public class OAuth2AuthFilter implements AuthChainFilter {

    private final OAuth2Repository oAuth2Repository;
    private final AuthFailedLimit authFailedLimit;
    private final UserAuthChecks userAuthChecks;
    private final DomainRepository domainRepository;

    @Autowired
    public OAuth2AuthFilter(OAuth2Repository oAuth2Repository, AuthFailedLimit authFailedLimit, UserAuthChecks userAuthChecks, DomainRepository domainRepository) {
        this.oAuth2Repository = oAuth2Repository;
        this.authFailedLimit = authFailedLimit;
        this.userAuthChecks = userAuthChecks;
        this.domainRepository = domainRepository;
    }

    @Override
    public Mono<AuthChainOneFilterResult> process(AuthenticateRequest authenticateRequest) {
        String password = authenticateRequest.getPassword();
        String username = authenticateRequest.getUsername();

        if (StringUtils.isEmpty(username) || !username.startsWith(AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            return Mono.just(EMPTY_RESULT);
        }

        return Mono.create(s -> {
            AuthChainOneFilterResult result = new AuthChainOneFilterResult();

            Mono<UserAccount> mono = oAuth2Repository.findUserByOAuth2TokenIDAndUserNameToken(password, username);
            mono.doOnSuccessOrError((user, y) -> {
                // no data
                if (user == null || y == null) {
                    authFailedLimit.processFailedAuth(authenticateRequest);
                    s.success(EMPTY_RESULT);
                } else {
                    List<String> auths;

                    // if username start with following characters - this is oauth auto
                    // generated username
                    if (username.startsWith(AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
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
                            if (token.getRoles().contains(SystemRoles.OAUTH_ALL_USER)) {
                                auths = user.getRoles();
                            }
                        } else {
                            auths = new ArrayList<>();
                        }

                        result.getAuthenticateResult().setDomain(domainRepository.findDomainById(user.getDomain()));

                        if (!StringUtils.isEmpty(authenticateRequest.getIp())) {
                            userAuthChecks.checkUserIpPatternAndActiveDomain(authenticateRequest.getIp(), user, result.getAuthenticateResult().getDomain());
                        }

                        userAuthChecks.checkUserEnabled(user, authenticateRequest);
                        auths.add(SystemRoles.OAUTH_AUTHENTICATED);

                        // do not add domain settings because they not always need
//                result.getAuthenticateResult().setDomainSettings(domainSettingsRepository.findDomainSettingsById(user.getDomain()));
                        result.getAuthenticateResult().setUserAccount(user);
                        result.getAuthenticateResult().setAuths(auths);
                        result.getAuthenticateResult().setAuthenticated(true);
                        result.setSuccessProcessed(true);
                    }

                    s.success(result);
                }
            });

        });
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
