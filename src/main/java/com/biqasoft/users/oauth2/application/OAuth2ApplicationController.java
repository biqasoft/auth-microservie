/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.oauth2.application;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "oauth2")
@RestController
@RequestMapping(value = "/v1/oauth2/application")
public class OAuth2ApplicationController {

    private final OAuth2ApplicationRepository oAuth2ApplicationRepositoryRepository;
    private final CurrentUser currentUser;
    private final OAuth2Repository oAuth2Repository;

    @Autowired
    public OAuth2ApplicationController(OAuth2ApplicationRepository oAuth2ApplicationRepositoryRepository, CurrentUser currentUser, OAuth2Repository oAuth2Repository) {
        this.currentUser = currentUser;
        this.oAuth2ApplicationRepositoryRepository = oAuth2ApplicationRepositoryRepository;
        this.oAuth2Repository = oAuth2Repository;
    }

    @ApiOperation(value = "find All Oauth Applications In Domain")
    @RequestMapping("list/domain")
    public List<OAuth2Application> findAllOauthApplicationsInDomain() {
        return oAuth2ApplicationRepositoryRepository.findAllOauthApplicationsInDomain();
    }

    @ApiOperation(value = "find All Public Oauth Applications")
    @RequestMapping("list/public")
    public List<OAuth2Application> findAllPublicOauthApplications() {
        return oAuth2ApplicationRepositoryRepository.findAllPublicOauthApplications();
    }

    @ApiOperation(value = "get meta info by oauth application by id")
    @RequestMapping("id/{id}")
    public OAuth2Application findOauthApplicationById(@PathVariable("id") String id) {
        return oAuth2ApplicationRepositoryRepository.findOauthApplicationById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "delete oauth application by id")
    @RequestMapping(value = "id/{id}", method = RequestMethod.DELETE)
    public void deleteOauthApplicationById(@PathVariable("id") String id){
        oAuth2ApplicationRepositoryRepository.deleteOauthApplicationById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "create New Application")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public OAuth2Application createNewApplication(@RequestBody OAuth2Application application) {
        application.setGiveAccessWithoutPrompt(false);
        application.setDomain(currentUser.getDomain().getDomain());
        return oAuth2ApplicationRepositoryRepository.createNewApplication(application);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "update oauth application")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public OAuth2Application updateApplication(@RequestBody OAuth2Application application){
        return oAuth2ApplicationRepositoryRepository.updateApplication(application);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "get application secret code by oauth application by id", notes = "Only user who create application can access this operation")
    @RequestMapping("{id}/secret_code")
    public SampleDataResponse getSecretCodeForOAuthApplication(@PathVariable("id") String id) {
        return oAuth2ApplicationRepositoryRepository.getSecretCodeForOAuthApplication(oAuth2ApplicationRepositoryRepository.findOauthApplicationById(id));
    }

    @RequestMapping(value = "obtain_access_code/{code}", method = RequestMethod.POST)
    public UserAccountOAuth2 obtainAccessCode(@RequestBody OAuth2Application application, @PathVariable("code") String code) {
        return oAuth2Repository.findUserWithAccessCodeAndApplicationAndSecretCode(application.getId(), code, application.getSecretCode());
    }

}
