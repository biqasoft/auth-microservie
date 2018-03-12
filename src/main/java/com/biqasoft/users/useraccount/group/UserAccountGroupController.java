/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.useraccount.group;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.users.authenticate.AuthHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Api(value = "User Accounts Groups")
@Secured(value = {SystemRoles.USER_GROUP_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/users/group")
public class UserAccountGroupController {

    private final UserAccountGroupRepository userAccountGroupRepository;

    @Autowired
    public UserAccountGroupController(UserAccountGroupRepository userAccountGroupRepository) {
        this.userAccountGroupRepository = userAccountGroupRepository;
    }

    @ApiOperation(value = "get all users groups in current domain")
    @GetMapping
    public List<UserAccountGroup> getAllUserAccountGroups(Principal principal) {
        return userAccountGroupRepository.findUserAccountGroupAll(AuthHelper.castFromPrincipal(principal));
    }

    @ApiOperation(value = "get group by id")
    @GetMapping(value = "{id}")
    public UserAccountGroup getUserAccountGroupById(@PathVariable("id") String id, Principal principal) {
        return userAccountGroupRepository.findUserAccountGroupById(id, AuthHelper.castFromPrincipal(principal));
    }

    @ApiOperation(value = "update user account")
    @PutMapping
    public UserAccountGroup updateUserAccountGroup(@RequestBody UserAccountGroup group, Principal principal){
        userAccountGroupRepository.updateUserAccountGroup(group, AuthHelper.castFromPrincipal(principal));
        return group;
    }

    @ApiOperation(value = "create user account")
    @PostMapping
    public UserAccountGroup createUserAccountGroup(@RequestBody UserAccountGroup group, Principal principal){
        userAccountGroupRepository.createUserAccountGroup(group, AuthHelper.castFromPrincipal(principal));
        return group;
    }

    @ApiOperation(value = "delete group by id")
    @DeleteMapping(value = "{id}")
    public void deleteUserAccountGroupById(@PathVariable("id") String id, Principal principal) {
        userAccountGroupRepository.deleteUserAccountGroup(id, AuthHelper.castFromPrincipal(principal));
    }

}
