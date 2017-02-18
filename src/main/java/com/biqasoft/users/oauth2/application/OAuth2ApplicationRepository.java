package com.biqasoft.users.oauth2.application;

import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.users.config.SystemSettings;

import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public interface OAuth2ApplicationRepository {
    OAuth2Application createNewApplication(OAuth2Application application);

    OAuth2Application findOauthApplicationById(String id);

    void deleteOauthApplicationById(String id);

    List<OAuth2Application> findAllPublicOauthApplications();

    List<OAuth2Application> findAllOauthApplicationsInDomain();

    OAuth2Application updateApplication(OAuth2Application application);

    SystemSettings getSystemSettings();

    OAuth2Application getSystemOAuthApplication();

    SampleDataResponse getSecretCodeForOAuthApplication(OAuth2Application application);

}
