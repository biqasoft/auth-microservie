/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.useraccount.group;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.users.useraccount.UserAccountRepository;
import com.biqasoft.users.useraccount.UserAccountRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain bound Groups
 * and users are stored in one database
 *
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/6/2016.
 * All Rights Reserved
 */
@Service
public class UserAccountGroupRepository {

    private final MongoOperations tenant;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUser currentUser;

    @Autowired
    public UserAccountGroupRepository(UserAccountRepository userAccountRepository, @TenantDatabase MongoOperations tenant, CurrentUser currentUser) {
        this.userAccountRepository = userAccountRepository;
        this.tenant = tenant;
        this.currentUser = currentUser;
    }

    /**
     * Check that user can not grant admin or root role
     * see similar {@link UserAccountRepositoryImpl#checkSystemRolesPermission(UserAccount)}
     *
     * @param userAccountGroup
     */
    private void checkRolesPermission(UserAccountGroup userAccountGroup) {

        if (!currentUser.getCurrentUser().getRoles().contains(SystemRoles.ROLE_ADMIN) &&
                !currentUser.getCurrentUser().getRoles().contains(SystemRoles.ROOT_USER)) {
            if (userAccountGroup.getGrantedRoles().contains(SystemRoles.ROLE_ADMIN)) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.invalid.only_admin_can_grant_role_admin");
            }
        }

        if (userAccountGroup.getGrantedRoles().contains(SystemRoles.ROOT_USER)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.invalid.only_admin_can_grant_role_root");
        }

    }

    /**
     * Add userAccountGroup to all users which should have it
     *
     * @param userAccountGroup
     */
    private void processGroupOperation(UserAccountGroup userAccountGroup) {
        checkRolesPermission(userAccountGroup);

        // TODO: test > 256 elements
        List<UserAccount> userAccounts = userAccountRepository.findAllUsersInDomain().toStream().collect(Collectors.toList());

        // delete this group from all users which have this group earlier
        for (UserAccount userAccount : userAccounts) {

            userAccount.setGroups(userAccount.getGroups().stream().
                    filter(x -> !x.getId().equals(userAccountGroup.getId()))
                    .collect(Collectors.toList()));
        }

        // if updated user account group have user account - add to user account his group
        for (String userThatWillHave : userAccountGroup.getUserAccountsIDs()) {

            for (UserAccount userAccount : userAccounts) {
                if (!userAccount.getId().equals(userThatWillHave)) {
                    continue;
                }
                userAccount.getGroups().add(userAccountGroup);
            }
        }

        for (UserAccount userAccount : userAccounts) {
            userAccountRepository.unsafeUpdateUserAccount(userAccount);
        }

    }

    /**
     * If user have userAccountGroup that we now want to delete - delete this groupd from all users
     *
     * @param userAccountGroup
     */
    private void processDeleteGroupOperation(UserAccountGroup userAccountGroup) {
        checkRolesPermission(userAccountGroup);

        List<UserAccount> userAccounts = userAccountRepository.findAllUsersInDomain().toStream().collect(Collectors.toList());

        // delete this group from all users which have this group earlier
        for (UserAccount userAccount : userAccounts) {

            userAccount.setGroups(userAccount.getGroups().stream().filter(x -> !x.getId().equals(userAccountGroup.getId())).collect(Collectors.toList()));
            userAccountRepository.unsafeUpdateUserAccount(userAccount);
        }
    }

    @BiqaAddObject
    @BiqaAuditObject
    public void createUserAccountGroup(UserAccountGroup userAccountGroup) {
        checkRolesPermission(userAccountGroup);
        tenant.insert(userAccountGroup);
    }

    @BiqaAuditObject
    public UserAccountGroup updateUserAccountGroup(UserAccountGroup userAccountGroup) {
        checkRolesPermission(userAccountGroup);

        tenant.save(userAccountGroup);

        processGroupOperation(userAccountGroup);
        return userAccountGroup;
    }

    public void deleteUserAccountGroup(String id) {
        UserAccountGroup group = findUserAccountGroupById(id);

        if (id == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.group.no_such");
        }

        processDeleteGroupOperation(group);
        tenant.remove(group);
    }

    public UserAccountGroup findUserAccountGroupById(String id) {
        Criteria criteria = new Criteria();
        criteria.and("id").is(id);

        Query query = new Query(criteria);
        return tenant.findOne(query, UserAccountGroup.class);
    }

    public List<UserAccountGroup> findUserAccountGroupAll() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);

        return tenant.find(query, UserAccountGroup.class);
    }

}
