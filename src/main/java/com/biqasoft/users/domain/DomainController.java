/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain;

import com.biqasoft.users.authenticate.AuthHelper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * {@link com.biqasoft.microservice.common.MicroserviceDomain}
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 10/5/2015
 *         All Rights Reserved
 */
@RestController
@Api("Control domains")
@RequestMapping("/v1/domain")
public class DomainController {

    private final DomainRepository domainRepository;

    @Autowired
    public DomainController(com.biqasoft.users.domain.DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Domain> findAllInDomainsUnsafe() {
        return domainRepository.findAllInDomainsUnsafe();
    }

    @GetMapping(value = "my")
    public Domain my(Principal principal) {
        return domainRepository.findDomainCurrentUser(AuthHelper.castFromPrincipal(principal));
    }

    @RequestMapping(method = RequestMethod.POST)
    public Domain create(@RequestBody Domain domain) {
        return domainRepository.addDomain(domain);
    }

    @RequestMapping(value = "{id}/unsafe", method = RequestMethod.DELETE)
    public void deleteUnsafe(@PathVariable("id") String id) {
        domainRepository.deleteDomainUnsafe(id);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "my", method = RequestMethod.PUT)
    public Domain updateDomainForCurrentUser(@RequestBody Domain domain, Principal principal) {
        return domainRepository.updateDomainForCurrentUser(domain, AuthHelper.castFromPrincipal(principal));
    }

    @RequestMapping(value = "unsafe", method = RequestMethod.PUT)
    public Domain updateDomainUnsafe(@RequestBody Domain domain) {
        return domainRepository.updateDomain(domain);
    }

    @RequestMapping(value = "{id}/unsafe", method = RequestMethod.GET)
    public Domain findDomainById(@PathVariable("id") String id) {
        return domainRepository.findDomainById(id);
    }

}