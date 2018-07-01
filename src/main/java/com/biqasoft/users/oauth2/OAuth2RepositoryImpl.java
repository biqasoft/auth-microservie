package com.biqasoft.users.oauth2;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.microservice.common.dto.oauth2.OAuth2NewTokenRequest;
import com.biqasoft.microservice.database.MainReactiveDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.oauth2.application.OAuth2ApplicationRepository;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.biqasoft.users.authenticate.AuthHelper.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX;

@Service
public class OAuth2RepositoryImpl implements OAuth2Repository {

    private final ReactiveMongoOperations ops;
    private final UserAccountRepository userAccountRepository;
    private final OAuth2ApplicationRepository oAuth2ApplicationRepository;

    private RandomString oauthTokenRandomString;
    private RandomString oauthUsernameRandomString;

    @Autowired
    public OAuth2RepositoryImpl(UserAccountRepository userAccountRepository, OAuth2ApplicationRepository oAuth2ApplicationRepository,
                                @MainReactiveDatabase ReactiveMongoOperations ops,
                                @Value("${biqa.auth.oauth.secret.code.length}") Integer oauthPasswordLength,
                                @Value("${biqa.auth.oauth.username.code.length}") Integer oauthUsernameLength) {
        this.userAccountRepository = userAccountRepository;
        this.oAuth2ApplicationRepository = oAuth2ApplicationRepository;
        this.ops = ops;

        this.oauthTokenRandomString = new RandomString(oauthPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS_ALL);
        this.oauthUsernameRandomString = new RandomString(oauthUsernameLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS);
    }

    @Override
    public Flux<UserAccountOAuth2> getCurrentUserTokens(CurrentUserCtx ctx) {
        return userAccountRepository.findByUserId(ctx.getUserAccount().getId(), ctx).flatMapIterable(userAccount -> {
            List<UserAccountOAuth2> tokens = new ArrayList<>(userAccount.getoAuth2s());
            tokens.forEach(x -> {
                x.setAccessCode(null);
                x.setAccessToken(null);
            });
            return tokens;
        });
    }

    @Override
    public Mono<UserAccount> findUserByOAuth2TokenIDAndUserNameToken(String tokenID, String customUsername) {
        return ops.findOne(Query.query(Criteria.where("oAuth2s.accessToken").is(tokenID).and("oAuth2s.userName").is(customUsername)), UserAccount.class);
    }

    @Override
    public Mono<UserAccountOAuth2> findUserWithAccessCodeAndApplicationAndSecretCode(String applicationID, String code, String applicationSecretCode) {
        return oAuth2ApplicationRepository.findOauthApplicationById(applicationID).flatMap(oAuth2Application -> {

            if (oAuth2Application == null) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_such_application");
            }

            if (!oAuth2Application.getSecretCode().equals(applicationSecretCode)) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.wrong_secret_code");
            }

            return ops.findOne(Query.query(Criteria.where("oAuth2s.accessCode").in(code).and("oAuth2s.clientApplicationID").in(applicationID)), UserAccount.class).flatMap(userAccount -> {

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
                return userAccountRepository.unsafeUpdateUserAccount(userAccount).map(l -> auth2);
            });
        });
    }

    @Override
    public Mono<UserAccountOAuth2> createNewOAuthToken(UserAccount userAccountRequested, OAuth2Application oAuth2Application, OAuth2NewTokenRequest request, CurrentUserCtx ctx) {
        UserAccountOAuth2 auth2 = new UserAccountOAuth2();
        auth2.setClientApplicationID(oAuth2Application.getId());
        return userAccountRepository.findByUserId(userAccountRequested.getId(), ctx).map(userAccount -> {

            String accessCode = oauthTokenRandomString.nextString();
            String userName = AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX + oauthUsernameRandomString.nextString();
            String password = oauthTokenRandomString.nextString();

            auth2.setUserName(userName);
            auth2.setAccessToken(password);
            auth2.setAccessCode(accessCode);

            // check that only roles, that user have
            // he assign (delegate) to role of oauth token
            List<String> roles = request.getRoles().stream()
                    .filter(x -> (ctx.getUserAccount().getRoles().contains(x) || x.equals(SystemRoles.OAUTH_ALL_USER))).collect(Collectors.toList());

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
        });

    }

    @Override
    public Mono<UserNameWithPassword> createAdditionalUsernameAndPasswordCredentialsOauth(UserAccount userAccount, List<String> rolesRequested, Date expireDate, CurrentUserCtx ctx) {
        UserNameWithPassword landingPageResponseDao = new UserNameWithPassword();

        OAuth2Application application = oAuth2ApplicationRepository.getSystemOAuthApplication();
        OAuth2NewTokenRequest request = new OAuth2NewTokenRequest();

        if (CollectionUtils.isEmpty(rolesRequested)) {
            List<String> roles = new ArrayList<>();
            roles.add(SystemRoles.OAUTH_ALL_USER);
            request.setRoles(roles);
        } else {
            request.setRoles(rolesRequested);
        }

        if (expireDate != null) {
            request.setExpire(expireDate);
        }

        return createNewOAuthToken(userAccount, application, request, ctx).map(userAccountOAuth2 -> {
            landingPageResponseDao.setUsername(userAccountOAuth2.getUserName());
            landingPageResponseDao.setPassword(userAccountOAuth2.getAccessToken());

            return landingPageResponseDao;
        });
    }

    @Override
    public Mono<Void> deleteOauthTokenFromUserAccountById(String userAccount, String tokenId, CurrentUserCtx ctx) {
        return userAccountRepository.findByUserId(userAccount, ctx).flatMap(account -> {
            account.setoAuth2s(account.getoAuth2s().stream().filter(x -> !x.getUserName().equals(tokenId)).collect(Collectors.toList()));
            return userAccountRepository.unsafeUpdateUserAccount(account).flatMap(l -> Mono.empty());
        });
    }

}