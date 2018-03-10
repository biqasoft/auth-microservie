/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.users.auth.CurrentUserContextProviderImpl;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.dto.UserAccountAddRequestDTO;
import com.google.common.collect.Lists;
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

    @GetMapping
    public List<com.biqasoft.entity.core.useraccount.UserAccount> findUnsafeFindAllUsers() {
        return UserAccountMapper.transform(userAccountRepository.unsafeFindAllUsers());
    }

    @GetMapping(value = "id/{id}")
    public com.biqasoft.entity.core.useraccount.UserAccount findUserById(@PathVariable("id") String id) {
        return UserAccountMapper.transform(userAccountRepository.unsafeFindUserById(id));
    }

    @GetMapping(value = "search/domain/id/{id}")
    public Domain findDomainForUserId(@PathVariable("id") String id) {
        UserAccount byUserId = userAccountRepository.findByUserId(id);
        String domainForInternalUser = CurrentUserContextProviderImpl.getDomainForInternalUser(byUserId);
        return domainRepository.findDomainById(domainForInternalUser);
    }

    @ApiOperation(value = "register new user in new domain with admin role")
    @PostMapping(value = "register")
    public CreatedUserDto register(@RequestBody UserAccountAddRequestDTO userAccountAddRequest) throws Exception {

        // user with same email already exist
        if (userAccountRepository.findByUsernameOrOAuthToken(userAccountAddRequest.getUserAccount().getEmail()) != null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("useraccount.create.username.already.exists");
        }

        Domain domain = domainRepository.addDomain(null);

        // create new admin account
        UserAccount user = new UserAccount();

        user.setTelephone(userAccountAddRequest.getUserAccount().getTelephone());
        user.setUsername(userAccountAddRequest.getUserAccount().getEmail());
        user.setFirstname(userAccountAddRequest.getUserAccount().getFirstname());
        user.setLastname(userAccountAddRequest.getUserAccount().getLastname());
        user.setEmail(userAccountAddRequest.getUserAccount().getEmail());

        user.setRoles(Lists.newArrayList(SystemRoles.ROLE_ADMIN, SystemRoles.ALLOW_ALL_DOMAIN_BASED));
        user.setDomain(domain.getDomain());

        CreatedUser createdUserInternal = userAccountRepository.registerNewUser(user);

        CreatedUserDto response = new CreatedUserDto();
        response.setDomain(createdUserInternal.getDomain());
        response.setPassword(createdUserInternal.getPassword());
        response.setUserAccount(UserAccountMapper.transform(createdUserInternal.getUserAccount()));

        if (userAccountAddRequest.isSendWelcomeEmail()) {
            emailPrepareAndSendService.sendWelcomeEmailWhenCreateNewDomain(createdUserInternal.getUserAccount(), createdUserInternal.getPassword());
        }
        return response;
    }

    @ApiOperation(value = "find user by username or oauth2 token ")
    @PostMapping(value = "find/username_or_oauth2_token")
    public com.biqasoft.entity.core.useraccount.UserAccount create(@RequestBody UserAccountGet userAccountGet) {
        return UserAccountMapper.transform(userAccountRepository.findByUsernameOrOAuthToken(userAccountGet.username));
    }

    @PutMapping
    public com.biqasoft.entity.core.useraccount.UserAccount updateUserAccount(@RequestBody com.biqasoft.entity.core.useraccount.UserAccount userPosted) {
        return UserAccountMapper.transform(userAccountRepository.unsafeUpdateUserAccount(UserAccountMapper.transform(userPosted)));
    }

    @DeleteMapping(value = "id/{id}")
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

    static class CreatedUserDto {
        private com.biqasoft.entity.core.useraccount.UserAccount userAccount;

        // do not add @JsonIgnore
        private String password;
        private String domain;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public com.biqasoft.entity.core.useraccount.UserAccount getUserAccount() {
            return userAccount;
        }

        public void setUserAccount(com.biqasoft.entity.core.useraccount.UserAccount userAccount) {
            this.userAccount = userAccount;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}