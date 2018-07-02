/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.oauth2.application;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.microservice.database.MainReactiveDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.config.SystemSettings;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 7/9/2016
 * All Rights Reserved
 */
@Service
public class OAuth2ApplicationRepositoryImpl implements OAuth2ApplicationRepository {

    private final ReactiveMongoOperations ops;

    private SystemSettings systemSettings = null;
    private OAuth2Application systemOAuthApplication = null;
    private final RandomString oauthTokenRandomString;

    public OAuth2ApplicationRepositoryImpl(@MainReactiveDatabase ReactiveMongoOperations ops,
                                           @Value("${biqa.auth.oauth.secret.code.length}") Integer oauthPasswordLength) {
        this.ops = ops;
        this.oauthTokenRandomString = new RandomString(oauthPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS_ALL);
    }

    @PostConstruct
    private void initSystemOAuthApplication() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);

        SystemSettings systemSettings = ops.findOne(query, SystemSettings.class).block();
        if (systemSettings == null || systemSettings.getSystemOAuthApplicationId() == null) {
            createNewSystemOAuthApplication();
        } else {
            OAuth2Application application = findOauthApplicationById(systemSettings.getSystemOAuthApplicationId()).block();

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
        roles.add(SystemRoles.OAUTH_ALL_USER);
        application.setRoles(roles);

        createNewApplicationPrivateWithoutChecking(application);

        systemSettings.setSystemOAuthApplicationId(application.getId());

        ops.save(systemSettings).block();
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

        application.setCreatedInfo(new CreatedInfo(LocalDateTime.now()));

        // set app secret code on app creation
        String secretCode = oauthTokenRandomString.nextString();
        application.setSecretCode(secretCode);

        ops.insert(application).block();
        return application;
    }

    @Override
    @BiqaAddObject
    public Mono<OAuth2Application> createNewApplication(OAuth2Application application) {

        // set app secret code on app creation
        String accessCode = oauthTokenRandomString.nextString();
        application.setSecretCode(accessCode);

        return ops.insert(application).map(x -> {
            return application;
        });
    }

    @Override
    public Mono<OAuth2Application> findOauthApplicationById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), OAuth2Application.class);
    }

    @Override
    public Mono<Boolean> deleteOauthApplicationById(String id, CurrentUserCtx ctx) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), OAuth2Application.class).flatMap(application -> {
            if (!application.getCreatedInfo().getCreatedById().equals(ctx.getUserAccount().getId())) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
            }
            return ops.remove(application).map(res -> res.getDeletedCount() > 0);
        });
    }

    @Override
    public Flux<OAuth2Application> findAllPublicOauthApplications() {
        return ops.find(Query.query(Criteria.where("publicApp").is(true)), OAuth2Application.class);
    }

    @Override
    public Flux<OAuth2Application> findAllOauthApplicationsInDomain(CurrentUserCtx ctx) {
        return ops.find(Query.query(Criteria.where("domain").is(ctx.getDomain().getDomain())), OAuth2Application.class);
    }

    @Override
    public Mono<OAuth2Application> updateApplication(OAuth2Application application, CurrentUserCtx ctx) {
        return findOauthApplicationById(application.getId()).flatMap(oAuth2Application -> {

            if (oAuth2Application == null) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.no_such_application");
            }

            if (oAuth2Application.getId().equals(getSystemOAuthApplication().getId())) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
            }

            if (!oAuth2Application.getCreatedInfo().getCreatedById().equals(ctx.getUserAccount().getId())) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.modify.only_creator_can");
            }

            oAuth2Application.setRedirect_uri(application.getRedirect_uri());
            oAuth2Application.setName(application.getName());
            oAuth2Application.setDescription(application.getDescription());
            oAuth2Application.setRoles(application.getRoles());
            oAuth2Application.setAvatarUrl(application.getAvatarUrl());
            oAuth2Application.setPublicApp(application.isPublicApp());

            return ops.save(oAuth2Application).map(l -> application);
        });
    }


    @Override
    public Mono<SampleDataResponseDto> getSecretCodeForOAuthApplication(OAuth2Application application, CurrentUserCtx ctx) {
        SampleDataResponseDto response = new SampleDataResponseDto();

        if (!application.getCreatedInfo().getCreatedById().equals(ctx.getUserAccount().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.secret_code.only_creator_can");
        }

        response.setData(application.getSecretCode());
        return Mono.just(response);
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
