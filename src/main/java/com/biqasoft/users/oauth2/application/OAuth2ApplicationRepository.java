package com.biqasoft.users.oauth2.application;

import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.auth.CurrentUserCtx;
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

    Mono<Boolean> deleteOauthApplicationById(String id, CurrentUserCtx ctx);

    Flux<OAuth2Application> findAllPublicOauthApplications();

    Flux<OAuth2Application> findAllOauthApplicationsInDomain(CurrentUserCtx ctx);

    Mono<OAuth2Application> updateApplication(OAuth2Application application, CurrentUserCtx ctx);

    SystemSettings getSystemSettings();

    OAuth2Application getSystemOAuthApplication();

    Mono<SampleDataResponseDto> getSecretCodeForOAuthApplication(OAuth2Application application, CurrentUserCtx ctx);

}
