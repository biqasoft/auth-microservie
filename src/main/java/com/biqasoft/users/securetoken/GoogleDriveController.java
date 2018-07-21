/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.securetoken;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.dto.httpresponse.LinkFieldDataResponse;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.users.authenticate.AuthHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@Api(value = "External common")
@Secured(value = {SystemRoles.EXTERNAL_SERVICES_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RequestMapping(value = "v1/token/gdrive")
@ConditionalOnProperty({"google.drive.CLIENT_ID_KEY", "biqa.REQUIRE_ALL"})
public class GoogleDriveController {

    private final String redirectLink;
    private final GoogleAuthService gDriveRepository;

    @Autowired
    public GoogleDriveController(GoogleAuthService gDriveRepository, @Value("${google.drive.auth.redirect.url}") String redirectLink) {
        this.gDriveRepository = gDriveRepository;
        this.redirectLink = redirectLink;
    }

    @Secured(value = {SystemRoles.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "first step to connect new account", notes = "return string ( url ) where user in browser should be redirected")
    @GetMapping("redirect_link")
    public Mono<LinkFieldDataResponse> getAuthLinkToConnectNewAccount() {
        LinkFieldDataResponse linkFieldDataResponse = new LinkFieldDataResponse();
        linkFieldDataResponse.setUrl(redirectLink);
        return Mono.just(linkFieldDataResponse);
    }

    @Secured(value = {SystemRoles.EXTERNAL_SERVICES_ADD_ACCOUNTS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "resolve in server aces token ")
    @GetMapping("oauth2/code/")
    public Mono<ExternalServiceToken> getAccessCode(@RequestParam("code") String code, Principal principal) {
        return gDriveRepository.obtainCodeToToken(AuthHelper.castFromPrincipal(principal), code);
    }

}

