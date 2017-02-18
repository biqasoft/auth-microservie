package com.biqasoft.users.oauth2;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.common.dto.OAuth2NewTokenRequest;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.oauth2.application.OAuth2ApplicationRepository;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OAuth2RepositoryImpl implements OAuth2Repository {

    private final CurrentUser currentUser;
    private final MongoOperations ops;
    private final UserAccountRepository userAccountRepository;
    private final OAuth2ApplicationRepository oAuth2ApplicationRepository;

    private RandomString oauthTokenRandomString;
    private RandomString oauthUsernameRandomString;

    @Autowired
    public OAuth2RepositoryImpl(CurrentUser currentUser, UserAccountRepository userAccountRepository, OAuth2ApplicationRepository oAuth2ApplicationRepository,
                                @MainDatabase MongoOperations ops,
                                @Value("${biqa.auth.oauth.secret.code.length}") Integer oauthPasswordLength,
                                @Value("${biqa.auth.oauth.username.code.length}") Integer oauthUsernameLength) {
        this.currentUser = currentUser;
        this.userAccountRepository = userAccountRepository;
        this.oAuth2ApplicationRepository = oAuth2ApplicationRepository;
        this.ops = ops;

        this.oauthTokenRandomString = new RandomString(oauthPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS_ALL);
        this.oauthUsernameRandomString = new RandomString(oauthUsernameLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS);
    }

    @Override
    public List<UserAccountOAuth2> getCurrentUserTokens(){
        UserAccount userAccount = userAccountRepository.findByUserId(currentUser.getCurrentUser().getId());

        List<UserAccountOAuth2> tokens = new ArrayList<>(userAccount.getoAuth2s());
        tokens.stream().map(x -> {
            x.setAccessCode(null);
            x.setAccessToken(null);
            return x;
        }).collect(Collectors.toList());
        return tokens;
    }

    @Override
    public UserAccount findUserByOAuth2TokenIDAndUserNameToken(String tokenID, String customUsername) {
        return ops.findOne(Query.query(Criteria.where("oAuth2s.accessToken").is(tokenID).and("oAuth2s.userName").is(customUsername)), UserAccount.class);
    }

    @Override
    public UserAccountOAuth2 findUserWithAccessCodeAndApplicationAndSecretCode(String applicationID, String code, String applicationSecretCode){

        OAuth2Application oAuth2Application = oAuth2ApplicationRepository.findOauthApplicationById(applicationID);
        if (oAuth2Application == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_such_application");
        }

        if (!oAuth2Application.getSecretCode().equals(applicationSecretCode)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.wrong_secret_code");
        }

        UserAccount userAccount = ops.findOne(Query.query(
                Criteria.where("oAuth2s.accessCode").in(code).and("oAuth2s.clientApplicationID").in(applicationID)), UserAccount.class);

        if (userAccount == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_with_code");
        }

        UserAccountOAuth2 auth2 = userAccount.getoAuth2s().stream()
                .filter(x -> x.getClientApplicationID().equals(applicationID) && x.getAccessCode().equals(code))
                .findFirst().get();

        if (auth2 == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_with_code");
        }

        auth2.setAccessCode(null);
        userAccountRepository.unsafeUpdateUserAccount(userAccount);

        return auth2;
    }

    @Override
    public UserAccountOAuth2 createNewOAuthToken(UserAccount userAccountRequested, OAuth2Application oAuth2Application, OAuth2NewTokenRequest request) {
        UserAccountOAuth2 auth2 = new UserAccountOAuth2();
        auth2.setClientApplicationID(oAuth2Application.getId());

        UserAccount userAccount = userAccountRepository.findByUserId(userAccountRequested.getId());

        String accessCode = oauthTokenRandomString.nextString();
        String userName = SYSTEM_CONSTS.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX + oauthUsernameRandomString.nextString();
        String password = oauthTokenRandomString.nextString();

        auth2.setUserName(userName);
        auth2.setAccessToken(password);
        auth2.setAccessCode(accessCode);

        // check that only roles, that user have
        // he assign (delegate) to role of oauth token
        List<String> roles = request.getRoles().stream()
                .filter(x -> (currentUser.getCurrentUser().getRoles().contains(x) || x.equals(SYSTEM_ROLES.OAUTH_ALL_USER))).collect(Collectors.toList());

        auth2.setRoles(roles);
        auth2.setEnabled(true);
        auth2.setCreatedInfo(new CreatedInfo(new Date()));

        if (request.getExpire() != null) {
            auth2.setExpire(request.getExpire());
        }

        if (userAccount.getoAuth2s() == null) userAccount.setoAuth2s(new ArrayList<>());

        userAccount.getoAuth2s().add(auth2);
        userAccountRepository.unsafeUpdateUserAccount(userAccount);

        return auth2;
    }

    @Override
    public UserNameWithPassword createAdditionalUsernameAndPasswordCredentialsOauth(UserAccount userAccount, List<String> rolesRequested, Date expireDate) {
        UserNameWithPassword landingPageResponseDao = new UserNameWithPassword();

        OAuth2Application application = oAuth2ApplicationRepository.getSystemOAuthApplication();
        OAuth2NewTokenRequest request = new OAuth2NewTokenRequest();

        if (CollectionUtils.isEmpty(rolesRequested)) {
            List<String> roles = new ArrayList<>();
            roles.add(SYSTEM_ROLES.OAUTH_ALL_USER);
            request.setRoles(roles);
        } else {
            request.setRoles(rolesRequested);
        }

        if (expireDate != null) {
            request.setExpire(expireDate);
        }

        UserAccountOAuth2 userAccountOAuth2 = createNewOAuthToken(userAccount, application, request);

        landingPageResponseDao.setUsername(userAccountOAuth2.getUserName());
        landingPageResponseDao.setPassword(userAccountOAuth2.getAccessToken());

        return landingPageResponseDao;
    }

    @Override
    public void deleteOauthTokenFromUserAccountById(String userAccount, String tokenId) {
        UserAccount account = userAccountRepository.findByUserId(userAccount);

        account.setoAuth2s(account.getoAuth2s().stream().filter(x -> !x.getUserName().equals(tokenId)).collect(Collectors.toList()));
        userAccountRepository.unsafeUpdateUserAccount(account);
    }

}