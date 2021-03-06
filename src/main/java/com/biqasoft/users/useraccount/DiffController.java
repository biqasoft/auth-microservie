/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.useraccount;

import com.biqasoft.audit.object.ObjectsAuditHistoryService;
import com.biqasoft.audit.object.diffs.ChangeObjectDTO;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "History objects")
@ApiIgnore
@RestController
@Secured(value = {SYSTEM_ROLES.HISTORY_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RequestMapping(value = "/v1/diff/history/objects")
public class DiffController {

    private final ObjectsAuditHistoryService objectsAuditHistoryService;

    @Autowired
    public DiffController(ObjectsAuditHistoryService objectsAuditHistoryService) {
        this.objectsAuditHistoryService = objectsAuditHistoryService;
    }

    @Secured(value = {SYSTEM_ROLES.HISTORY_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get history info of customer changes just as string formatted")
    @RequestMapping(value = "class/{className}/id/{id}", method = RequestMethod.GET)
    public List<ChangeObjectDTO> getChanges(HttpServletResponse response, @PathVariable("id") String id, @PathVariable("className") String className) {
        return ObjectsAuditHistoryService.transformJaversChangesToDTO(objectsAuditHistoryService.getChangesByObject(com.biqasoft.users.useraccount.UserAccount.class, id, "main"));
    }

}
