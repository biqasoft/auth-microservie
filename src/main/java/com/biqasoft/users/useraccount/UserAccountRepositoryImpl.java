package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.microservice.database.MainReactiveDatabase;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.group.UserAccountGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final CurrentUser currentUser;
    private final ReactiveMongoOperations ops;
    private final PasswordEncoder passwordEncoder;
    private final RandomString randomPassword;

    @Autowired
    public UserAccountRepositoryImpl(@MainReactiveDatabase ReactiveMongoOperations ops, CurrentUser currentUser, PasswordEncoder passwordEncoder,
                                     @Value("${biqa.auth.password.default.length}") Integer defaultPasswordLength) {
        this.ops = ops;
        this.currentUser = currentUser;
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
     * see {@link UserAccountGroupRepository#checkRolesPermission(UserAccountGroup)}
     *
     * @param userAccount
     * @throws Exception
     */
    private void checkDomainRolesPermission(UserAccount userAccount) throws Exception {
        List<String> currentUserRoles = currentUser.getCurrentUser().getRoles();
        if (!currentUserRoles.contains(SystemRoles.ROLE_ADMIN) && !currentUserRoles.contains(SystemRoles.ROOT_USER)) {
            if (userAccount.getRoles().contains(SystemRoles.ROLE_ADMIN)) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.grant.role.admin.error");
            }
        }
    }

    private void checkSystemRolesPermission(UserAccount userAccount) throws Exception {
        if (userAccount.getRoles().contains(SystemRoles.ROOT_USER)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.grant.role.root.error");
        }
    }

    /**
     * You can not create account with username that start with oauth token prefix
     *
     * @param userAccount
     */
    private void checkUserAccountRulesRequirements(UserAccount userAccount) {
        if (userAccount.getUsername().startsWith(SYSTEM_CONSTS.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.create.username.forbidden.symbols");
        }
    }

    @Override
    public Mono<CreatedUser> registerNewUser(UserAccount userAccount) throws Exception {
        return create(userAccount, true);
    }

    @Override
    @BiqaAddObject
    @BiqaAuditObject
    public Mono<CreatedUser> createUserAccountInDomain(UserAccount userAccount, String password) throws Exception {
        userAccount.setPassword(password); //dirty hack
        return create(userAccount, false);
    }

    private Mono<CreatedUser> create(UserAccount userAccount, boolean anonymous) throws Exception {

        if (anonymous) {
            checkSystemRolesPermission(userAccount);
        } else {
            checkSystemRolesPermission(userAccount);
            checkDomainRolesPermission(userAccount);
        }

        // duplicate username
        if (findByUsernameOrOAuthToken(userAccount.getUsername()) != null) {
            // this text of exception is used on client
            // don't edit it
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.create.username.already.exists");
        }

        checkUserAccountRulesRequirements(userAccount);

        if (StringUtils.isEmpty(userAccount.getPassword())) {
            userAccount.setPassword(randomPassword.nextString());
        }

        String clearTextPassword = userAccount.getPassword();

        userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
        userAccount.setEnabled(true); // or false
        userAccount.setStatus(UserAccount.UserAccountStatus.STATUS_APPROVED.name()); // or UserAccountStatus.STATUS_DISABLED

        userAccount.setCreatedInfo(new CreatedInfo(new Date()));
        return ops.insert(userAccount).map(x -> {
            CreatedUser createdUser = new CreatedUser();
            createdUser.setUserAccount(userAccount);
            createdUser.setPassword(clearTextPassword);
            createdUser.setDomain(userAccount.getDomain());
            return createdUser;
        });
    }

    @Override
    public Mono<UserAccount> findByUserId(String user) {
        return ops.findOne(Query.query(Criteria.where("id").is(user).and("domain").is(currentUser.getDomain().getDomain())), UserAccount.class);
    }

    @Override
    public Mono<UserAccount> unsafeFindUserById(String user) {
        return ops.findOne(Query.query(Criteria.where("id").is(user)), UserAccount.class);
    }

    @Override
    public Mono<UserAccount> findByUsernameOrOAuthToken(String userName) {

        // is this is oauth2 token
        if (userName.startsWith(SYSTEM_CONSTS.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            return findUserByOAuth2UserNameToken(userName);
        } else {
            return ops.findOne(Query.query(Criteria.where("username").is(userName)), UserAccount.class);
        }
    }

    @Override
    public Mono<UserAccount> findUserByOAuth2UserNameToken(String customUsername) {
        return ops.findOne(Query.query(Criteria.where("oAuth2s.userName").is(customUsername)), UserAccount.class);
    }

    @Override
    public Mono<UserAccount> unsafeUpdateUserAccount(UserAccount userAccount) {
        return ops.save(userAccount);
    }

    @Override
    @BiqaAuditObject
    public Mono<UserAccount> updateUserAccountForCurrentDomain(UserAccount userAccount) throws Exception {
        checkDomainRolesPermission(userAccount);
        findByUserId(userAccount.getId()).flatMap(oldUserAccount -> {

            // do not allow to override some system important fields
            if (oldUserAccount.getDomain().equals(currentUser.getDomain().getDomain())) {
                userAccount.setPassword(oldUserAccount.getPassword());
                userAccount.setoAuth2s(oldUserAccount.getoAuth2s());
                userAccount.setDomain(oldUserAccount.getDomain());
                return ops.save(userAccount).map(l -> userAccount);
            } else {
                return Mono.empty();
            }
        });
        return Mono.empty();
    }

    @Override
    public Mono<Void> setCurrentUserOnline() {
        Query query = new Query(Criteria.where("id").is(currentUser.getCurrentUser().getId()));
        Update update = new Update().set("lastOnline", new Date());
        return ops.findAndModify(query, update, UserAccount.class).flatMap(x -> Mono.empty());
    }

    @Override
    public Mono<Void> setCurrentUserPersonalSettings(PersonalSettings personalSettings) {
        Query query = new Query(Criteria.where("id").is(currentUser.getCurrentUser().getId()));
        Update update = new Update().set("personalSettings", personalSettings);
        return ops.findAndModify(query, update, UserAccount.class).flatMap(x -> Mono.empty());
    }

    @Override
    public Flux<UserAccount> findAllUsersInDomain() {
        return ops.find(Query.query(Criteria.where("domain").is(currentUser.getDomain().getDomain())), UserAccount.class);
    }

    @Override
    public Flux<UserAccount> unsafeFindAllUsers() {
        return ops.find(new Query(new Criteria()), UserAccount.class);
    }

    @Override
    public Mono<Void> deleteUserById(String username) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(username);

        return ops.remove(userAccount).flatMap(x -> Mono.empty());
    }

    @Override
    public Flux<UserAccount> fullTextSearch(UserSearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Criteria criteeria = Criteria.where("domain").is(currentUser.getDomain().getDomain());
        Query query = TextQuery.queryText(criteria).sortByScore().addCriteria(criteeria);

        return ops.find(query, UserAccount.class);
    }

}