/*
 * Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dto.TwoStepModifyRequest;
import com.biqasoft.users.useraccount.dto.UserAccountAddRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final CurrentUser currentUser;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final UserSecondFactorService userSecondFactorService;

    public DomainUserController(UserAccountRepository userAccountRepository, CurrentUser currentUser, EmailPrepareAndSendService emailPrepareAndSendService, UserSecondFactorService userSecondFactorService) {
        this.userAccountRepository = userAccountRepository;
        this.currentUser = currentUser;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.userSecondFactorService = userSecondFactorService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "")
    public Flux<com.biqasoft.entity.core.useraccount.UserAccount> findAllInDomainsUnsafe() {
        return userAccountRepository.findAllUsersInDomain().map(UserAccountMapper::mapInternalToDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "2step")
    public UserSecondFactorService.SecondFactorResponseDTO twoStepAuth() {
        return userSecondFactorService.generateSecret2FactorForUser();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "2step/modify")
    public void twoStepAuth(@RequestBody TwoStepModifyRequest twoStepModifyRequest) {
        userSecondFactorService.tryToChange2FactorMode(twoStepModifyRequest.isEnabled(), twoStepModifyRequest.getCode());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "current_user/set_online")
    public Mono<Void> setOnline() {
        return userAccountRepository.setCurrentUserOnline();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "id/{id}")
    public Mono<com.biqasoft.entity.core.useraccount.UserAccount> findUserById(@PathVariable("id") String id) {
        return userAccountRepository.findByUserId(id).map(UserAccountMapper::mapInternalToDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "fulltext_search")
    public Flux<com.biqasoft.entity.core.useraccount.UserAccount> search(@RequestBody UserSearchRequest searchRequest) {
        return userAccountRepository.fullTextSearch(searchRequest).map(UserAccountMapper::mapInternalToDto);
    }

    @ApiOperation(value = "add new user")
    @PostMapping(value = "")
    public Mono<com.biqasoft.entity.core.useraccount.UserAccount> create(@RequestBody UserAccountAddRequestDTO userAccountAddRequest) throws Exception {
        UserAccount userPosted = userAccountAddRequest.getUserAccount();
        userPosted.setDomain(currentUser.getDomain().getDomain());

        return userAccountRepository.createUserAccountInDomain(userPosted, userAccountAddRequest.getPassword()).map(createdUser -> {

            if (userAccountAddRequest.isSendWelcomeEmail()) {
                emailPrepareAndSendService.sendWelcomeEmailWhenAddNewUserToDomain(userPosted, createdUser.getPassword());
            }

            return UserAccountMapper.mapInternalToDto(createdUser.getUserAccount());
        });
    }

    @PutMapping
    public Mono<com.biqasoft.entity.core.useraccount.UserAccount> deleteUserAccount(@RequestBody UserAccount userPosted) throws Exception {
        return userAccountRepository.updateUserAccountForCurrentDomain(userPosted).map(UserAccountMapper::mapInternalToDto);
    }

    @PutMapping(value = "current_user/personal_settings")
    public void deleteUserAccount(@RequestBody PersonalSettings personalSettings) {
        userAccountRepository.setCurrentUserPersonalSettings(personalSettings);
    }

    @DeleteMapping(value = "id/{id}")
    public Mono<Void> deleteUserAccount(@PathVariable("id") String id) {
        return userAccountRepository.findByUserId(id).flatMap(userAccount -> {

            if (!userAccount.getDomain().equals(currentUser.getDomain().getDomain())) {
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

