package com.biqasoft.users.oauth2.application;

import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.config.SystemSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         All Rights Reserved
 */
public interface OAuth2ApplicationRepository {

    Mono<OAuth2Application> createNewApplication(OAuth2Application application);

    Mono<OAuth2Application> findOauthApplicationById(String id);

    Mono<Boolean> deleteOauthApplicationById(String id);

    Flux<OAuth2Application> findAllPublicOauthApplications();

    Flux<OAuth2Application> findAllOauthApplicationsInDomain();

    Mono<OAuth2Application> updateApplication(OAuth2Application application);

    SystemSettings getSystemSettings();

    OAuth2Application getSystemOAuthApplication();

    Mono<SampleDataResponse> getSecretCodeForOAuthApplication(OAuth2Application application);

}
