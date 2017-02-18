/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.useraccount.group;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "User Accounts Groups")
@Secured(value = {SYSTEM_ROLES.USER_GROUP_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/users/group")
public class UserAccountGroupController {

    private final UserAccountGroupRepository userAccountGroupRepository;

    @Autowired
    public UserAccountGroupController(UserAccountGroupRepository userAccountGroupRepository) {
        this.userAccountGroupRepository = userAccountGroupRepository;
    }

    @ApiOperation(value = "get all users groups in current domain")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<UserAccountGroup> getAllUserAccountGroups() {
        return userAccountGroupRepository.findUserAccountGroupAll();
    }

    @ApiOperation(value = "get group by id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public UserAccountGroup getUserAccountGroupById(@PathVariable("id") String id) {
        return userAccountGroupRepository.findUserAccountGroupById(id);
    }

    @ApiOperation(value = "update user account")
    @RequestMapping(method = RequestMethod.PUT)
    public UserAccountGroup updateUserAccountGroup(@RequestBody UserAccountGroup group){
        userAccountGroupRepository.updateUserAccountGroup(group);
        return group;
    }

    @ApiOperation(value = "create user account")
    @RequestMapping(method = RequestMethod.POST)
    public UserAccountGroup createUserAccountGroup(@RequestBody UserAccountGroup group){
        userAccountGroupRepository.createUserAccountGroup(group);
        return group;
    }

    @ApiOperation(value = "delete group by id")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteUserAccountGroupById(@PathVariable("id") String id) {
        userAccountGroupRepository.deleteUserAccountGroup(id);
    }

}
