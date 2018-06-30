/*
 * Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.users.domain.useraccount.PersonalSettings;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dto.TwoStepModifyRequest;
import com.biqasoft.users.useraccount.dto.UserAccountAddRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceUsersRepository}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 10/5/2015
 * All Rights Reserved
 */
@RestController
@Api("User accounts management in domain")
@RequestMapping("/v1/users/domain")
public class DomainUserController {

    private final UserAccountRepository userAccountRepository;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final UserSecondFactorService userSecondFactorService;

    public DomainUserController(UserAccountRepository userAccountRepository, EmailPrepareAndSendService emailPrepareAndSendService, UserSecondFactorService userSecondFactorService) {
        this.userAccountRepository = userAccountRepository;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.userSecondFactorService = userSecondFactorService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "")
    public Flux<com.biqasoft.users.domain.useraccount.UserAccount> findAllInDomainsUnsafe(Principal principal) {
        return userAccountRepository.findAllUsersInDomain(AuthHelper.castFromPrincipal(principal)).map(UserAccountMapper::mapInternalToDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "2step")
    public UserSecondFactorService.SecondFactorResponseDTO twoStepAuth(Principal principal) {
        return userSecondFactorService.generateSecret2FactorForUser(AuthHelper.castFromPrincipal(principal));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "2step/modify")
    public void twoStepAuth(@RequestBody TwoStepModifyRequest twoStepModifyRequest, Principal principal) {
        userSecondFactorService.tryToChange2FactorMode(twoStepModifyRequest.isEnabled(), twoStepModifyRequest.getCode(), AuthHelper.castFromPrincipal(principal));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "current_user/set_online")
    public Mono<Void> setOnline(Principal principal) {
        return userAccountRepository.setCurrentUserOnline(AuthHelper.castFromPrincipal(principal));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "id/{id}")
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> findUserById(@PathVariable("id") String id, Principal principal) {
        return userAccountRepository.findByUserId(id, AuthHelper.castFromPrincipal(principal)).map(UserAccountMapper::mapInternalToDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "fulltext_search")
    public Flux<com.biqasoft.users.domain.useraccount.UserAccount> search(@RequestBody UserSearchRequest searchRequest, Principal principal) {
        return userAccountRepository.fullTextSearch(searchRequest, AuthHelper.castFromPrincipal(principal)).map(UserAccountMapper::mapInternalToDto);
    }

    @ApiOperation(value = "add new user")
    @PostMapping(value = "")
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> create(@RequestBody UserAccountAddRequestDTO userAccountAddRequest, Principal principal) throws Exception {
        UserAccount userPosted = userAccountAddRequest.getUserAccount();
        userPosted.setDomain(AuthHelper.castFromPrincipal(principal).getDomain().getDomain());

        return userAccountRepository.createUserAccountInDomain(userPosted, userAccountAddRequest.getPassword(), AuthHelper.castFromPrincipal(principal)).map(createdUser -> {

            if (userAccountAddRequest.isSendWelcomeEmail()) {
                emailPrepareAndSendService.sendWelcomeEmailWhenAddNewUserToDomain(userPosted, createdUser.getPassword());
            }

            return UserAccountMapper.mapInternalToDto(createdUser.getUserAccount());
        });
    }

    @PutMapping
    public Mono<com.biqasoft.users.domain.useraccount.UserAccount> deleteUserAccount(@RequestBody UserAccount userPosted, Principal principal) throws Exception {
        return userAccountRepository.updateUserAccountForCurrentDomain(userPosted, AuthHelper.castFromPrincipal(principal)).map(UserAccountMapper::mapInternalToDto);
    }

    @PutMapping(value = "current_user/personal_settings")
    public void deleteUserAccount(@RequestBody PersonalSettings personalSettings, Principal principal) {
        userAccountRepository.setCurrentUserPersonalSettings(personalSettings, AuthHelper.castFromPrincipal(principal));
    }

    @DeleteMapping(value = "id/{id}")
    public Mono<Void> deleteUserAccount(@PathVariable("id") String id, Principal principal) {
        return userAccountRepository.findByUserId(id, AuthHelper.castFromPrincipal(principal)).flatMap(userAccount -> {

            if (!userAccount.getDomain().equals(AuthHelper.castFromPrincipal(principal).getDomain().getDomain())) {
                ThrowExceptionHelper.throwExceptionInvalidRequest("Access deny");
            }

            return userAccountRepository.deleteUserById(id);
        });
    }


}

class UserSearchRequest {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

