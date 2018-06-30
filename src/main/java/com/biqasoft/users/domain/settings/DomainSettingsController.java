/*
* Copyright (c) 2016 biqasoft.com
 */

package com.biqasoft.users.domain.settings;

import com.biqasoft.users.domain.DomainSettings;
import com.biqasoft.users.authenticate.AuthHelper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceDomain}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("Control domains")
@RequestMapping("/v1/domain_settings")
@Deprecated(forRemoval = true)
public class DomainSettingsController {

    private final DomainSettingsRepository domainSettingsRepository;

    @Autowired
    public DomainSettingsController(DomainSettingsRepository domainSettingsRepository) {
        this.domainSettingsRepository = domainSettingsRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "my",method = RequestMethod.GET)
    public DomainSettings my(Principal principal) {
        return domainSettingsRepository.findDomainSettingsCurrentUser(AuthHelper.castFromPrincipal(principal));
    }

    @RequestMapping(method = RequestMethod.POST)
    public DomainSettings create(@RequestBody DomainSettings domainSettings) {
        return domainSettingsRepository.addDomainSettings(domainSettings);
    }

    @RequestMapping(value = "id/{id}", method = RequestMethod.DELETE)
    public void deleteUnsafe(@PathVariable("id") String id) {
        domainSettingsRepository.delete(id);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "domain", method = RequestMethod.PUT)
    public DomainSettings updateDomainForCurrentUser(@RequestBody DomainSettings domainSettings, Principal principal) {
        return domainSettingsRepository.updateDomainSettings(domainSettings, AuthHelper.castFromPrincipal(principal));
    }

    @RequestMapping(value = "unsafe", method = RequestMethod.PUT)
    public DomainSettings updateDomainSettings(@RequestBody DomainSettings domainSettings) {
        return domainSettingsRepository.update(domainSettings);
    }

    @RequestMapping(value = "id/{id}/unsafe", method = RequestMethod.GET)
    public DomainSettings findDomainById(@PathVariable("id") String id) {
        return domainSettingsRepository.findDomainSettingsById(id);
    }

}