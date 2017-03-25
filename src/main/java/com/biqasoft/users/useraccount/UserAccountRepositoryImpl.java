package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.SYSTEM_CONSTS;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.group.UserAccountGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final CurrentUser currentUser;
    private final MongoOperations ops;
    private final PasswordEncoder passwordEncoder;
    private final RandomString randomPassword;

    @Autowired
    public UserAccountRepositoryImpl(@MainDatabase MongoOperations ops, CurrentUser currentUser, PasswordEncoder passwordEncoder,
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
        if (!currentUserRoles.contains(SYSTEM_ROLES.ROLE_ADMIN) && !currentUserRoles.contains(SYSTEM_ROLES.ROOT_USER)) {
            if (userAccount.getRoles().contains(SYSTEM_ROLES.ROLE_ADMIN)) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.grant.role.admin.error");
            }
        }
    }

    private void checkSystemRolesPermission(UserAccount userAccount) throws Exception {
        if (userAccount.getRoles().contains(SYSTEM_ROLES.ROOT_USER)) {
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
    public CreatedUser registerNewUser(UserAccount userAccount) throws Exception {
        return create(userAccount, true);
    }

    @Override
    @BiqaAddObject
    @BiqaAuditObject
    public CreatedUser createUserAccountInDomain(UserAccount userAccount, String password) throws Exception {
        userAccount.setPassword(password); //dirty hack
        return create(userAccount, false);
    }

    private CreatedUser create(UserAccount userAccount, boolean anonymous) throws Exception {

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
        ops.insert(userAccount);

        CreatedUser createdUser = new CreatedUser();
        createdUser.setUserAccount(userAccount);
        createdUser.setPassword(clearTextPassword);
        createdUser.setDomain(userAccount.getDomain());

        return createdUser;
    }

    @Override
    public UserAccount findByUserId(String user) {
        return ops.findOne(Query.query(Criteria.where("id").is(user).and("domain").is(currentUser.getDomain().getDomain())), UserAccount.class);
    }

    @Override
    public UserAccount unsafeFindUserById(String user) {
        return ops.findOne(Query.query(Criteria.where("id").is(user)), UserAccount.class);
    }

    @Override
    public UserAccount findByUsernameOrOAuthToken(String userName) {

        // is this is oauth2 token
        if (userName.startsWith(SYSTEM_CONSTS.AUTHENTIFICATION_OAUTH2_USERNAME_PREFIX)) {
            return findUserByOAuth2UserNameToken(userName);
        } else {
            return ops.findOne(Query.query(Criteria.where("username").is(userName)), UserAccount.class);
        }
    }

    @Override
    public UserAccount findUserByOAuth2UserNameToken(String customUsername) {
        return ops.findOne(Query.query(Criteria.where("oAuth2s.userName").is(customUsername)), UserAccount.class);
    }

    @Override
    public UserAccount unsafeUpdateUserAccount(UserAccount userAccount) {
        ops.save(userAccount);
        return userAccount;
    }

    @Override
    @BiqaAuditObject
    public UserAccount updateUserAccountForCurrentDomain(UserAccount userAccount) throws Exception {
        checkDomainRolesPermission(userAccount);
        UserAccount oldUserAccount = findByUserId(userAccount.getId());

        // do not allow to override some system important fields
        if (oldUserAccount.getDomain().equals(currentUser.getDomain().getDomain())) {
            userAccount.setPassword(oldUserAccount.getPassword());
            userAccount.setoAuth2s(oldUserAccount.getoAuth2s());
            userAccount.setDomain(oldUserAccount.getDomain());
            ops.save(userAccount);
        }
        return userAccount;
    }

    @Override
    public void setCurrentUserOnline() {
        Query query = new Query(Criteria.where("id").is(currentUser.getCurrentUser().getId()));
        Update update = new Update().set("lastOnline", new Date());
        ops.findAndModify(query, update, UserAccount.class);
    }

    @Override
    public void setCurrentUserPersonalSettings(PersonalSettings personalSettings) {
        Query query = new Query(Criteria.where("id").is(currentUser.getCurrentUser().getId()));
        Update update = new Update().set("personalSettings", personalSettings);
        ops.findAndModify(query, update, UserAccount.class);
    }

    @Override
    public List<UserAccount> findAllUsersInDomain() {
        return ops.find(Query.query(Criteria.where("domain").is(currentUser.getDomain().getDomain())), UserAccount.class);
    }

    @Override
    public List<UserAccount> unsafeFindAllUsers() {
        return ops.find(new Query(new Criteria()), UserAccount.class);
    }

    @Override
    public void deleteUserById(String username) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(username);

        ops.remove(userAccount);
    }

    @Override
    public List<UserAccount> fullTextSearch(UserSearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Criteria criteeria = Criteria.where("domain").is(currentUser.getDomain().getDomain());
        Query query = TextQuery.queryText(criteria).sortByScore().addCriteria(criteeria);

        return ops.find(query, UserAccount.class);
    }

}