package com.biqasoft.users.useraccount;

import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.users.useraccount.dto.CreatedUser;

import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public interface UserAccountRepository {

    UserAccount findUserByOAuth2UserNameToken(String customUsername);

    CreatedUser registerNewUser(UserAccount userAccount) throws Exception;

    CreatedUser createUserAccountInDomain(UserAccount userAccount, String password) throws Exception;

        /**
         * Find user account by id only in current domain
         *
         * @param user
         * @return
         */
    UserAccount findByUserId(String user);

    /**
     * Find user account by id
     *
     * @param user
     * @return
     */
    UserAccount unsafeFindUserById(String user);

    /**
     * Find user by username
     * or if this username start with OAuth2 token prefix
     * find account by token
     *
     * @param userName username or token
     * @return user account with this credentials
     */
    UserAccount findByUsernameOrOAuthToken(String userName);

    /**
     * Private API
     * this is unsafe save - only for internal use
     * usually you should use @code {unsafeUpdateUserAccount}
     *
     * @param newObject
     * @return
     */
    UserAccount unsafeUpdateUserAccount(UserAccount newObject);

    /**
     * Update user accounts which are in the same domain
     * that is requested user
     *
     * @param newObject
     * @return
     * @throws Exception
     */
    UserAccount updateUserAccountForCurrentDomain(UserAccount newObject) throws Exception;

    /**
     * Set current user as online
     */
    void setCurrentUserOnline();

    /**
     * Update personal settings (this is serialised JSON by browser which he stores settings with UI etc...)
     * for current user
     *
     * @param personalSettings
     */
    void setCurrentUserPersonalSettings(PersonalSettings personalSettings);

    /**
     * Find all users in current domain
     *
     * @return
     */
    List<UserAccount> findAllUsersInDomain();

    List<UserAccount> unsafeFindAllUsers();

    /**
     * FOR INTERNAL USAGE ONLY
     * Delete user by username
     *
     * @param id
     */
    void deleteUserById(String id);

    List<UserAccount> fullTextSearch(UserSearchRequest searchRequest);
}
