package com.biqasoft.users.useraccount;

import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.microservice.database.MainReactiveDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.useraccount.PersonalSettings;
import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.group.UserAccountGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.biqasoft.users.authenticate.AuthHelper.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX;

@Service
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final ReactiveMongoOperations ops;
    private final PasswordEncoder passwordEncoder;
    private final RandomString randomPassword;

    @Autowired
    public UserAccountRepositoryImpl(@MainReactiveDatabase ReactiveMongoOperations ops, PasswordEncoder passwordEncoder,
                                     @Value("${biqa.auth.password.default.length}") Integer defaultPasswordLength) {
        this.ops = ops;
        this.passwordEncoder = passwordEncoder;
        this.randomPassword = new RandomString(defaultPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS_ALL);
    }

    /**
     * check that new/updated user account
     * can be updated with current user
     * <p>
     * User can add ROLE_ADMIN to another user only
     * If he:
     * 1) ROLE_ADMIN OR
     * 2) ROLE_ROOT
     * <p>
     * see {@link UserAccountGroupRepository checkRolesPermission(UserAccountGroup)}
     *
     * @param userAccount
     */
    private void checkDomainRolesPermission(UserAccountDbo userAccount, CurrentUserCtx ctx) {
        List<String> currentUserRoles = ctx.getUserAccount().getRoles();
        if (!currentUserRoles.contains(SystemRoles.ROLE_ADMIN) && !currentUserRoles.contains(SystemRoles.ROOT_USER)) {
            if (userAccount.getRoles().contains(SystemRoles.ROLE_ADMIN)) {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.grant.role.admin.error");
            }
        }
    }

    private void checkSystemRolesPermission(UserAccountDbo userAccount) {
        if (userAccount.getRoles().contains(SystemRoles.ROOT_USER)) {
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.grant.role.root.error");
        }
    }

    /**
     * You can not create account with username that start with oauth token prefix
     *
     * @param userAccount
     */
    private void checkUserAccountRulesRequirements(UserAccountDbo userAccount) {
        if (userAccount.getUsername().startsWith(AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.create.username.forbidden.symbols");
        }
    }

    @Override
    public Mono<CreatedUser> registerNewUser(UserAccountDbo userAccount) {
        return create(userAccount, true, null);
    }

    @Override
    @BiqaAddObject
    @BiqaAuditObject
    public Mono<CreatedUser> createUserAccountInDomain(UserAccountDbo userAccount, String password, CurrentUserCtx ctx) {
        userAccount.setPassword(password); //dirty hack
        return create(userAccount, false, ctx);
    }

    private Mono<CreatedUser> create(UserAccountDbo userAccount, boolean anonymous, @Nullable CurrentUserCtx ctx) {

        if (anonymous) {
            checkSystemRolesPermission(userAccount);
        } else {
            checkSystemRolesPermission(userAccount);
            checkDomainRolesPermission(userAccount, ctx);
        }

        return findByUsernameOrOAuthToken(userAccount.getUsername())
                .flatMap(x -> ThrowAuthExceptionHelper.throwErrorReactiveBiqaLocalizedException("useraccount.create.username.already.exists"))

                // hack: map just to fix mono type
                .map(x -> new CreatedUser())
                .switchIfEmpty(Mono.defer(() -> {
                    checkUserAccountRulesRequirements(userAccount);

                    if (StringUtils.isEmpty(userAccount.getPassword())) {
                        userAccount.setPassword(randomPassword.nextString());
                    }

                    String clearTextPassword = userAccount.getPassword();

                    userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
                    userAccount.setEnabled(true); // or false
                    userAccount.setStatus(UserAccountDbo.UserAccountStatus.STATUS_APPROVED.name()); // or UserAccountStatus.STATUS_DISABLED

                    userAccount.setCreatedInfo(new CreatedInfo(LocalDateTime.now()));
                    return ops.insert(userAccount).flatMap(x -> {
                        CreatedUser createdUser = new CreatedUser();
                        createdUser.setUserAccount(userAccount);
                        createdUser.setPassword(clearTextPassword);
                        createdUser.setDomain(userAccount.getDomain());
                        return Mono.just(createdUser);
                    });
                }));
    }

    @Override
    public Mono<UserAccountDbo> findByUserId(String user, CurrentUserCtx ctx) {
        return ops.findOne(Query.query(Criteria.where("id").is(user).and("domain").is(ctx.getDomain().getDomain())), UserAccountDbo.class);
    }

    @Override
    public Mono<UserAccountDbo> unsafeFindUserById(String user) {
        return ops.findOne(Query.query(Criteria.where("id").is(user)), UserAccountDbo.class);
    }

    @Override
    public Mono<UserAccountDbo> findByUsernameOrOAuthToken(String userName) {

        // is this is oauth2 token
        if (userName.startsWith(AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            return findUserByOAuth2UserNameToken(userName);
        } else {
            return ops.findOne(Query.query(Criteria.where("username").is(userName)), UserAccountDbo.class);
        }
    }

    @Override
    public Mono<UserAccountDbo> findUserByOAuth2UserNameToken(String customUsername) {
        return ops.findOne(Query.query(Criteria.where("oAuth2s.userName").is(customUsername)), UserAccountDbo.class);
    }

    @Override
    public Mono<UserAccountDbo> unsafeUpdateUserAccount(UserAccountDbo userAccount) {
        return ops.save(userAccount);
    }

    @Override
    @BiqaAuditObject
    public Mono<UserAccountDbo> updateUserAccountForCurrentDomain(UserAccountDbo userAccount, CurrentUserCtx ctx) {
        checkDomainRolesPermission(userAccount, ctx);
        return findByUserId(userAccount.getId(), ctx).flatMap(oldUserAccount -> {

            // do not allow to override some system important fields
            if (oldUserAccount.getDomain().equals(ctx.getDomain().getDomain())) {
                userAccount.setPassword(oldUserAccount.getPassword());
                userAccount.setOAuth2s(oldUserAccount.getOAuth2s());
                userAccount.setDomain(oldUserAccount.getDomain());
                return ops.save(userAccount).map(l -> userAccount);
            } else {
                return Mono.empty();
            }
        });
    }

    @Override
    public Mono<Void> setCurrentUserOnline(CurrentUserCtx ctx) {
        Query query = new Query(Criteria.where("id").is(ctx.getUserAccount().getId()));
        Update update = new Update().set("lastOnline", new Date());
        return ops.findAndModify(query, update, UserAccountDbo.class).flatMap(x -> Mono.empty());
    }

    @Override
    public Mono<Void> setCurrentUserPersonalSettings(PersonalSettings personalSettings, CurrentUserCtx ctx) {
        Query query = new Query(Criteria.where("id").is(ctx.getUserAccount().getId()));
        Update update = new Update().set("personalSettings", personalSettings);
        return ops.findAndModify(query, update, UserAccountDbo.class).flatMap(x -> Mono.empty());
    }

    @Override
    public Flux<UserAccountDbo> findAllUsersInDomain(CurrentUserCtx ctx) {
        return ops.find(Query.query(Criteria.where("domain").is(ctx.getDomain().getDomain())), UserAccountDbo.class);
    }

    @Override
    public Flux<UserAccountDbo> unsafeFindAllUsers() {
        return ops.find(new Query(new Criteria()), UserAccountDbo.class);
    }

    @Override
    public Mono<Void> deleteUserById(String username) {
        UserAccountDbo userAccount = new UserAccountDbo();
        userAccount.setId(username);

        return ops.remove(userAccount).flatMap(x -> Mono.empty());
    }

    @Override
    public Flux<UserAccountDbo> fullTextSearch(DomainUserController.UserSearchRequest searchRequest, CurrentUserCtx ctx) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Criteria criteeria = Criteria.where("domain").is(ctx.getDomain().getDomain());
        Query query = TextQuery.queryText(criteria).sortByScore().addCriteria(criteeria);

        return ops.find(query, UserAccountDbo.class);
    }

}