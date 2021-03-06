package com.biqasoft.users.oauth2;

import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.common.dto.OAuth2NewTokenRequest;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.useraccount.UserAccount;

import java.util.Date;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public interface OAuth2Repository {
    UserAccount findUserByOAuth2TokenIDAndUserNameToken(String tokenID, String customUsername);

    List<UserAccountOAuth2> getCurrentUserTokens();

    UserAccountOAuth2 findUserWithAccessCodeAndApplicationAndSecretCode(String applicationID, String code, String applicationSecretCode);

    /**
     * POTENTIAL RISK METHOD
     *
     * @param userAccount
     * @param oAuth2Application
     * @param request
     * @return
     */
    UserAccountOAuth2 createNewOAuthToken(UserAccount userAccount, OAuth2Application oAuth2Application, OAuth2NewTokenRequest request);

    UserNameWithPassword createAdditionalUsernameAndPasswordCredentialsOauth(UserAccount userAccount, List<String> rolesRequested, Date expireDate);

    void deleteOauthTokenFromUserAccountById(String userAccount, String tokenId);
}
