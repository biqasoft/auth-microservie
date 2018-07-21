/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.securetoken;

import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Google Drive
 * Storage Integration
 * <p>
 * <p>
 * {@code https://security.google.com/settings/security/permissions}
 * {@code https://developers.google.com/drive/v2/reference/parents/get}
 * {@code https://developers.google.com/drive/web/folder}
 * {@code https://developers.google.com/drive/v2/reference/files}
 * {@code https://developers.google.com/drive/v2/reference/files/list}
 * {@code https://developers.google.com/drive/web/manage-downloads}
 * {@code https://developers.google.com/apis-explorer/#p/drive/v2/}
 * {@code https://developers.google.com/drive/v2/reference/files/get}
 */
@Service
@ConditionalOnProperty({"google.drive.CLIENT_ID_KEY", "biqa.REQUIRE_ALL"})
public class GoogleAuthService {

    @Value("${biqasoft.httpclient.name}")
    private String biqaHttpClientName;

    @Value("${google.drive.CLIENT_ID_KEY}")
    private String googleDriveClientId;

    @Value("${google.drive.CLIENT_SECRET}")
    private String googleDriveClientSecret;

    @Value("${google.drive.REDIRECT_URI_KEY}")
    private String googleDriveRedirectURI;

    private final static String GOOGLE_DRIVE_TOKEN_URI = "https://www.googleapis.com/oauth2/v3/token";

    private final ExternalServiceTokenRepository externalServiceTokenRepository;

    public GoogleAuthService(ExternalServiceTokenRepository externalServiceTokenRepository) {
        this.externalServiceTokenRepository = externalServiceTokenRepository;
    }

    public Mono<ExternalServiceToken> obtainCodeToToken(CurrentUserCtx ctx, String code) {
        WebClient webClient = WebClient.create(GOOGLE_DRIVE_TOKEN_URI);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", googleDriveClientId);
        form.add("client_secret", googleDriveClientSecret);
        form.add("redirect_uri", googleDriveRedirectURI);
        form.add("grant_type", "authorization_code");

        BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(form);

        // application/json to fix on exceptions
        return webClient.post().header("Accept", "application/json").body(multipartInserter).exchange()
                .flatMap(x -> {
                    if (x.statusCode().is4xxClientError()) {
                        throw new BiqaAuthenticationLocalizedException("authentication.failed");
                    }

                    return x.bodyToMono(GoogleOauthResponseToken.class);
                }).map(x -> {
                    if (StringUtils.isEmpty(x.getAccess_token())) {
                        throw new BiqaAuthenticationLocalizedException("authentication.failed");
                    }
                    return x;
                }).flatMap(re -> {

                    ExternalServiceToken externalServiceToken = new ExternalServiceToken();
                    externalServiceToken.setType(TOKEN_TYPES.GOOGLE_DRIVE);
                    externalServiceToken.setToken(re.getAccess_token());
                    externalServiceToken.setRefreshToken(re.getRefresh_token());

                    DateTime dateTime = new DateTime(new Date());
                    dateTime.plusSeconds(re.getExpires_in());
                    // google token have expires
                    externalServiceToken.setExpired(dateTime.toDate());
//
//                    Drive drive = getDriveClient(externalServiceToken, true);
//
//                    try {
//                        externalServiceToken.setName(drive.about().get().execute().getName());
//                        externalServiceToken.setLogin(drive.about().get().execute().getUser().getEmailAddress());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e.getMessage());
//                    }

                    return externalServiceTokenRepository.findExternalServiceTokenByLoginAndTypeIgnoreExpired(ctx, externalServiceToken.getLogin(),
                            TOKEN_TYPES.GOOGLE_DRIVE)
                            .switchIfEmpty(
                                    // if we DON'T already have user with the same login
                                    // add new token
                                    externalServiceTokenRepository.addExternalServiceToken(ctx, externalServiceToken))
                            .flatMap(existingTokenWithSameLogin -> {
                                // if we have token with current user
                                // but we have new refresh token
                                // we would update our token info
                                if (re.getRefresh_token() != null && !re.getRefresh_token().equals("")) {
                                    externalServiceToken.setId(existingTokenWithSameLogin.getId());
                                    externalServiceToken.setDomain(existingTokenWithSameLogin.getDomain());
                                    externalServiceToken.setCreatedInfo(new CreatedInfo(LocalDateTime.now(), ctx.getUserAccount().getId()));
                                    return externalServiceTokenRepository.updateExternalServiceToken(ctx, externalServiceToken);
                                } else {
                                    // we already have this user, but have not
                                    // refresh token - bad situation
                                    // which should not be...
                                }
                                // otherwise
                                // if we have not refresh token from google
                                // and we already have user with this login
                                // we would not do anything
                                return Mono.just(externalServiceToken);

                            });

                });
    }

    /**
     * See more at
     * <p>
     * {@code https://developers.google.com/identity/protocols/OAuth2WebServer}
     * {@code https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=ya29.wwGA_WB3GbyB4P6ExslSRMh5Ae6ObXyWmVBPK_pLSAj7NpXFg9h3OFDPhcRVY3_drhYy}
     */
    private Mono<ExternalServiceToken> refreshAccessCodeFromRefreshToken(ExternalServiceToken externalServiceToken, CurrentUserCtx ctx) {
        // this is only for google token
        if (!TOKEN_TYPES.GOOGLE_DRIVE.equals(externalServiceToken.getType())) return Mono.empty();

        // this method is only update, not create new
        return externalServiceTokenRepository.findExternalServiceTokenByIdIgnoreExpired(ctx, externalServiceToken.getId()).flatMap(t -> {

            LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

            form.add("refresh_token", externalServiceToken.getRefreshToken());
            form.add("client_id", googleDriveClientId);
            form.add("client_secret", googleDriveClientSecret);
            form.add("grant_type", "refresh_token");

            WebClient webClient = WebClient.create(GOOGLE_DRIVE_TOKEN_URI);

            BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(form);

            // application/json to fix on exceptions
            return webClient.post().header("Accept", "application/json").body(multipartInserter).exchange()
                    .flatMap(x -> {
                        if (x.statusCode().is4xxClientError()) {
                            throw new BiqaAuthenticationLocalizedException("authentication.failed");
                        }

                        return x.bodyToMono(GoogleOauthResponseToken.class);
                    }).map(x -> {
                        if (StringUtils.isEmpty(x.getAccess_token())) {
                            throw new BiqaAuthenticationLocalizedException("authentication.failed");
                        }
                        return x;
                    }).flatMap(re -> {
                        externalServiceToken.setToken(re.getAccess_token());

                        DateTime dateTime = new DateTime(new Date());
                        dateTime = dateTime.plusSeconds(re.getExpires_in());
                        externalServiceToken.setExpired(dateTime.toDate());

                        return externalServiceTokenRepository.updateExternalServiceToken(ctx, externalServiceToken);

                    });
        });
    }
}
