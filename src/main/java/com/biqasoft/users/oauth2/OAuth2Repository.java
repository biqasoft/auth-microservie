package com.biqasoft.users.oauth2;

import com.biqasoft.microservice.common.dto.oauth2.OAuth2NewTokenRequest;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public interface OAuth2Repository {
    Mono<UserAccount> findUserByOAuth2TokenIDAndUserNameToken(String tokenID, String customUsername);

    Flux<UserAccountOAuth2> getCurrentUserTokens(CurrentUserCtx ctx);

    Mono<UserAccountOAuth2> findUserWithAccessCodeAndApplicationAndSecretCode(String applicationID, String code, String applicationSecretCode);

    /**
     * POTENTIAL RISK METHOD
     *
     * @param userAccount
     * @param oAuth2Application
     * @param request
     * @return
     */
    Mono<UserAccountOAuth2> createNewOAuthToken(UserAccount userAccount, OAuth2Application oAuth2Application, OAuth2NewTokenRequest request, CurrentUserCtx ctx);

    Mono<UserNameWithPassword> createAdditionalUsernameAndPasswordCredentialsOauth(UserAccount userAccount, List<String> rolesRequested, Date expireDate, CurrentUserCtx ctx);

    Mono<Void> deleteOauthTokenFromUserAccountById(String userAccount, String tokenId, CurrentUserCtx ctx);
}
