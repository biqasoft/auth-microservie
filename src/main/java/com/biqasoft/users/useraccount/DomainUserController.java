/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dto.CreatedUser;
import com.biqasoft.users.useraccount.dto.UserAccountAddRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceUsersRepository}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("User accounts management in domain")
@RequestMapping("/v1/users/domain")
public class DomainUserController {

    private final UserAccountRepository userAccountRepository;
    private final CurrentUser currentUser;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final UserSecondFactorService userSecondFactorService;

    @Autowired
    public DomainUserController(UserAccountRepository userAccountRepository, CurrentUser currentUser, EmailPrepareAndSendService emailPrepareAndSendService, UserSecondFactorService userSecondFactorService) {
        this.userAccountRepository = userAccountRepository;
        this.currentUser = currentUser;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.userSecondFactorService = userSecondFactorService;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<com.biqasoft.entity.core.useraccount.UserAccount> findAllInDomainsUnsafe() {
        return UserAccountMapper.transform(userAccountRepository.findAllUsersInDomain());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "2step", method = RequestMethod.POST)
    public UserSecondFactorService.SecondFactorResponse twoStepAuth() {
        return userSecondFactorService.processRequest();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "2step/modify", method = RequestMethod.POST)
    public void twoStepAuth(@RequestBody TwoStepModifyRequest twoStepModifyRequest) {
        userSecondFactorService.modifyUserTwoStep(twoStepModifyRequest.isEnabled(), twoStepModifyRequest.getCode());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "current_user/set_online", method = RequestMethod.GET)
    public void setOnline() {
        userAccountRepository.setCurrentUserOnline();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "id/{id}", method = RequestMethod.GET)
    public com.biqasoft.entity.core.useraccount.UserAccount findUserById(@PathVariable("id") String id) {
        return UserAccountMapper.transform(userAccountRepository.findByUserId(id));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "fulltext_search", method = RequestMethod.POST)
    public List<com.biqasoft.entity.core.useraccount.UserAccount> search(@RequestBody UserSearchRequest searchRequest) {
       return UserAccountMapper.transform(userAccountRepository.fullTextSearch(searchRequest));
    }

    @ApiOperation(value = "add new user")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public com.biqasoft.entity.core.useraccount.UserAccount create(@RequestBody UserAccountAddRequestDTO userAccountAddRequest, HttpServletResponse response) throws Exception {
        UserAccount userPosted = userAccountAddRequest.getUserAccount();
        userPosted.setDomain(currentUser.getDomain().getDomain());

        CreatedUser createdUser = userAccountRepository.createUserAccountInDomain(userPosted, userAccountAddRequest.getPassword());

        if (userAccountAddRequest.isSendWelcomeEmail()) {
            emailPrepareAndSendService.sendWelcomeEmailWhenAddNewUserToDomain(userPosted, createdUser.getPassword());
        }

       return UserAccountMapper.transform(createdUser.getUserAccount());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public com.biqasoft.entity.core.useraccount.UserAccount deleteUserAccount(@RequestBody UserAccount userPosted) throws Exception {
        return UserAccountMapper.transform(userAccountRepository.updateUserAccountForCurrentDomain(userPosted));
    }

    @RequestMapping(value = "current_user/personal_settings", method = RequestMethod.PUT)
    public void deleteUserAccount(@RequestBody PersonalSettings personalSettings) throws Exception {
        userAccountRepository.setCurrentUserPersonalSettings(personalSettings);
    }

    @RequestMapping(value = "id/{id}",method = RequestMethod.DELETE)
    public void deleteUserAccount(@PathVariable("id") String id) throws Exception {
        UserAccount userAccount = userAccountRepository.findByUserId(id);

        if (!userAccount.getDomain().equals(currentUser.getDomain().getDomain())){
            ThrowExceptionHelper.throwExceptionInvalidRequest("Access deny");
        }

        userAccountRepository.deleteUserById(id);
    }


}
class TwoStepModifyRequest {
    private boolean enabled;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

class UserSearchRequest{
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

