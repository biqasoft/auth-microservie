/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.oauth2.application;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.config.SystemSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/9/2016
 *         All Rights Reserved
 */
@Service
public class OAuth2ApplicationRepositoryImpl implements OAuth2ApplicationRepository {

    private final CurrentUser currentUser;
    private final MongoOperations ops;

    private SystemSettings systemSettings = null;
    private OAuth2Application systemOAuthApplication = null;
    private final RandomString oauthTokenRandomString;

    @Autowired
    public OAuth2ApplicationRepositoryImpl(CurrentUser currentUser, @MainDatabase MongoOperations ops,
                                           @Value("${biqa.auth.oauth.secret.code.length}") Integer oauthPasswordLength) {
        this.currentUser = currentUser;
        this.ops = ops;
        this.oauthTokenRandomString = new RandomString(oauthPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS_ALL);
    }

    @PostConstruct
    private void initSystemOAuthApplication() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);

        SystemSettings systemSettings = ops.findOne(query, SystemSettings.class);
        if (systemSettings == null || systemSettings.getSystemOAuthApplicationId() == null) {
            createNewSystemOAuthApplication();
        } else {
            OAuth2Application application = findOauthApplicationById(systemSettings.getSystemOAuthApplicationId());

            if (application == null || application.getId() == null) {
                createNewSystemOAuthApplication();
            } else {
                this.systemSettings = systemSettings;
                this.systemOAuthApplication = application;
            }
        }

    }

    /**
     * Once init System application which will provide credentials
     * for all users
     */
    private void createNewSystemOAuthApplication() {
        SystemSettings systemSettings = new SystemSettings();

        OAuth2Application application = new OAuth2Application();
        application.setName("SYSTEM APPLICATION");
        application.setPublicApp(true);
        application.setGiveAccessWithoutPrompt(true);

        List<String> roles = new ArrayList<>();
        roles.add(SYSTEM_ROLES.OAUTH_ALL_USER);
        application.setRoles(roles);

        createNewApplicationPrivateWithoutChecking(application);

        systemSettings.setSystemOAuthApplicationId(application.getId());

        ops.save(systemSettings);
        this.systemSettings = systemSettings;
        this.systemOAuthApplication = application;
    }

    /**
     * do not add {@link BiqaAddObject}
     *
     * @param application
     * @return
     */
    private OAuth2Application createNewApplicationPrivateWithoutChecking(OAuth2Application application) {

        application.setCreatedInfo(new CreatedInfo(new Date()));

        // set app secret code on app creation
        String secretCode = oauthTokenRandomString.nextString();
        application.setSecretCode(secretCode);

        ops.insert(application);
        return application;
    }

    @Override
    @BiqaAddObject
    public OAuth2Application createNewApplication(OAuth2Application application) {

        // set app secret code on app creation
        String accessCode = oauthTokenRandomString.nextString();
        application.setSecretCode(accessCode);

        ops.insert(application);
        return application;
    }

    @Override
    public OAuth2Application findOauthApplicationById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), OAuth2Application.class);
    }

    @Override
    public void deleteOauthApplicationById(String id) {
        OAuth2Application application = ops.findOne(Query.query(Criteria.where("id").is(id)), OAuth2Application.class);

        if (!application.getCreatedInfo().getCreatedById().equals(currentUser.getCurrentUser().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
        }

        ops.remove(application);
    }

    @Override
    public List<OAuth2Application> findAllPublicOauthApplications() {
        return ops.find(Query.query(Criteria.where("publicApp").is(true)), OAuth2Application.class);
    }

    @Override
    public List<OAuth2Application> findAllOauthApplicationsInDomain() {
        return ops.find(Query.query(Criteria.where("domain").is(currentUser.getDomain().getDomain())), OAuth2Application.class);
    }

    @Override
    public OAuth2Application updateApplication(OAuth2Application application) {
        OAuth2Application oAuth2Application = findOauthApplicationById(application.getId());
        if (oAuth2Application == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_such_application");
        }

        if (oAuth2Application.getId().equals(getSystemOAuthApplication().getId())){
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
        }

        if (!oAuth2Application.getCreatedInfo().getCreatedById().equals(currentUser.getCurrentUser().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
        }

        oAuth2Application.setRedirect_uri(application.getRedirect_uri());
        oAuth2Application.setName(application.getName());
        oAuth2Application.setDescription(application.getDescription());
        oAuth2Application.setRoles(application.getRoles());
        oAuth2Application.setAvatarUrl(application.getAvatarUrl());
        oAuth2Application.setPublicApp(application.isPublicApp());

        ops.save(oAuth2Application);
        return application;
    }


    @Override
    public SampleDataResponse getSecretCodeForOAuthApplication(OAuth2Application application){
        SampleDataResponse response = new SampleDataResponse();

        if (!application.getCreatedInfo().getCreatedById().equals(currentUser.getCurrentUser().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.secret_code.only_creator_can");
        }

        response.setData(application.getSecretCode());
        return response;
    }

    @Override
    public SystemSettings getSystemSettings() {
        return systemSettings;
    }

    @Override
    public OAuth2Application getSystemOAuthApplication() {
        return systemOAuthApplication;
    }
}
