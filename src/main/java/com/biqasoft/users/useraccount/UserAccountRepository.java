package com.biqasoft.users.useraccount;

import com.biqasoft.users.domain.useraccount.PersonalSettings;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public interface UserAccountRepository {

    Mono<UserAccount> findUserByOAuth2UserNameToken(String customUsername);

    Mono<CreatedUser> registerNewUser(UserAccount userAccount, CurrentUserCtx ctx) throws Exception;

    Mono<CreatedUser> createUserAccountInDomain(UserAccount userAccount, String password, CurrentUserCtx ctx) throws Exception;

        /**
         * Find user account by id only in current domain
         *
         * @param user
         * @return
         */
    Mono<UserAccount> findByUserId(String user, CurrentUserCtx ctx);

    /**
     * Find user account by id
     *
     * @param user
     * @return
     */
    Mono<UserAccount> unsafeFindUserById(String user);

    /**
     * Find user by username
     * or if this username start with OAuth2 token prefix
     * find account by token
     *
     * @param userName username or token
     * @return user account with this credentials
     */
    Mono<UserAccount> findByUsernameOrOAuthToken(String userName);

    /**
     * Private API
     * this is unsafe save - only for internal use
     * usually you should use @code {unsafeUpdateUserAccount}
     *
     * @param newObject
     * @return
     */
    Mono<UserAccount> unsafeUpdateUserAccount(UserAccount newObject);

    /**
     * Update user accounts which are in the same domain
     * that is requested user
     *
     * @param newObject
     * @return
     * @throws Exception
     */
    Mono<UserAccount> updateUserAccountForCurrentDomain(UserAccount newObject, CurrentUserCtx ctx) throws Exception;

    /**
     * Set current user as online
     */
    Mono<Void> setCurrentUserOnline(CurrentUserCtx ctx);

    /**
     * Update personal settings (this is serialised JSON by browser which he stores settings with UI etc...)
     * for current user
     *
     * @param personalSettings
     */
    Mono<Void> setCurrentUserPersonalSettings(PersonalSettings personalSettings, CurrentUserCtx ctx);

    /**
     * Find all users in current domain
     *
     * @return
     */
    Flux<UserAccount> findAllUsersInDomain(CurrentUserCtx ctx);

    Flux<UserAccount> unsafeFindAllUsers();

    /**
     * FOR INTERNAL USAGE ONLY
     * Delete user by username
     *
     * @param id
     */
    Mono<Void> deleteUserById(String id);

    Flux<UserAccount> fullTextSearch(UserSearchRequest searchRequest, CurrentUserCtx ctx);
}
