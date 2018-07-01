/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

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

    @Autowired
    public GlobalUserController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public Flux<com.biqasoft.users.domain.useraccount.UserAccount> findUnsafeFindAllUsers() {
        return userAccountRepository.unsafeFindAllUsers().map(UserAccountMapper::mapInternalToDto);
    }

    @GetMapping(value = "id/{id}")
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> findUserById(@PathVariable("id") String id) {
        return userAccountRepository.unsafeFindUserById(id).map(UserAccountMapper::mapInternalToDto);
    }

    @GetMapping(value = "search/domain/id/{id}")
    public Domain findDomainForUserId(@PathVariable("id") String id, Principal principal) {
        // TODO:
        UserAccount byUserId = userAccountRepository.findByUserId(id, AuthHelper.castFromPrincipal(principal)).block();
//        String domainForInternalUser = CurrentUserContextProviderImpl.getDomainForInternalUser(byUserId);
//        return domainRepository.findDomainById(domainForInternalUser);
        return null;
    }

    @ApiOperation(value = "find user by username or oauth2 token ")
    @PostMapping(value = "find/username_or_oauth2_token")
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> create(@RequestBody UserAccountGet userAccountGet) {
        return userAccountRepository.findByUsernameOrOAuthToken(userAccountGet.username).map(UserAccountMapper::mapInternalToDto);
    }

    @PutMapping
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> updateUserAccount(@RequestBody com.biqasoft.users.domain.useraccount.UserAccount userPosted) {
        return userAccountRepository.unsafeUpdateUserAccount(UserAccountMapper.mapDtoToInternal(userPosted)).map(UserAccountMapper::mapInternalToDto);
    }

    @DeleteMapping(value = "id/{id}")
    public void updateUserAccount(@PathVariable("id") String id) throws Exception {
        userAccountRepository.deleteUserById(id);
    }


    @Data
    static class UserAccountGet {
        public String username;
    }

    @Data
    static class CreatedUserDto {
        private com.biqasoft.users.domain.useraccount.UserAccount userAccount;

        // do not add @JsonIgnore
        private String password;
        private String domain;

    }

}