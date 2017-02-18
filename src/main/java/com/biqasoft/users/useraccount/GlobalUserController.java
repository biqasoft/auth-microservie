/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.entity.core.Domain;
import com.biqasoft.users.auth.CurrentUserContextProviderImpl;
import com.biqasoft.users.auth.TransformUserAccountEntity;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.dto.UserAccountAddRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceUsersRepository}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("System User accounts management")
@RequestMapping("/v1/users")
public class GlobalUserController {

    private final UserAccountRepository userAccountRepository;
    private final DomainRepository domainRepository;
    private final EmailPrepareAndSendService emailPrepareAndSendService;

    @Autowired
    public GlobalUserController(UserAccountRepository userAccountRepository, DomainRepository domainRepository, EmailPrepareAndSendService emailPrepareAndSendService) {
        this.userAccountRepository = userAccountRepository;
        this.domainRepository = domainRepository;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
    }

    @RequestMapping(value = "")
    public List<com.biqasoft.entity.core.useraccount.UserAccount> findUnsafeFindAllUsers() {
        return TransformUserAccountEntity.transform(userAccountRepository.unsafeFindAllUsers());
    }

    @RequestMapping(value = "id/{id}")
    public com.biqasoft.entity.core.useraccount.UserAccount findUserById(@PathVariable("id") String id) {
        return TransformUserAccountEntity.transform(userAccountRepository.unsafeFindUserById(id));
    }

    @RequestMapping(value = "search/domain/id/{id}")
    public Domain findDomainForUserId(@PathVariable("id") String id) {
        UserAccount byUserId = userAccountRepository.findByUserId(id);
        String domainForInternalUser = CurrentUserContextProviderImpl.getDomainForInternalUser(byUserId);
        return domainRepository.findDomainById(domainForInternalUser);
    }

    @ApiOperation(value = "register new user in new domain")
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public CreatedUser register(@RequestBody UserAccountAddRequestDTO userAccountAddRequest) throws Exception {
        UserAccount userPosted = userAccountAddRequest.getUserAccount(); // this is already internal representation of domain
        userPosted.setDomain(userAccountAddRequest.getDomain());

        CreatedUser createdUser = userAccountRepository.registerNewUser(userPosted);

        if (userAccountAddRequest.isSendWelcomeEmail()) {
            emailPrepareAndSendService.sendWelcomeEmailWhenCreateNewDomain(userPosted, createdUser.getPassword());
        }
        return createdUser;
    }

    @ApiOperation(value = "find user by username or oauth2 token ")
    @RequestMapping(value = "find/username_or_oauth2_token", method = RequestMethod.POST)
    public com.biqasoft.entity.core.useraccount.UserAccount create(@RequestBody UserAccountGet userAccountGet) {
        return TransformUserAccountEntity.transform(userAccountRepository.findByUsernameOrOAuthToken(userAccountGet.username));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public com.biqasoft.entity.core.useraccount.UserAccount updateUserAccount(@RequestBody UserAccount userPosted) throws Exception {
        return TransformUserAccountEntity.transform(userAccountRepository.unsafeUpdateUserAccount(userPosted));
    }

    @RequestMapping(value = "id/{id}", method = RequestMethod.DELETE)
    public void updateUserAccount(@PathVariable("id") String id) throws Exception {
        userAccountRepository.deleteUserById(id);
    }


    static class UserAccountGet {
        public String username;

        public UserAccountGet(String username) {
            this.username = username;
        }

        public UserAccountGet() {
        }
    }

}