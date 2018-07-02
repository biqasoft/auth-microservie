/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount;

import com.biqasoft.entity.core.BaseClass;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javers.core.metamodel.annotation.DiffIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("This is main user account object")
public class UserAccount extends BaseClass {

    @ApiModelProperty("Username - login")
    private String username;

    @ApiModelProperty("telephone")
    private String telephone;

    // just some user defined variables
    private String firstname;

    private String lastname;

    private String patronymic;

    private String position;

    @ApiModelProperty(value = "user email", notes = " by default this is username")
    private String email;

    // spring security
    @ApiModelProperty("is approved (enabled) profile")
    private String status;

    // spring security
    @ApiModelProperty("Does account enabled")
    private Boolean enabled;

    // list of Java Spring Security roles
    @ApiModelProperty("List of roles - API allowed operations")
    private List<String> roles = new ArrayList<>();

    //@ApiModelProperty(value = "String which contains serialized JSON", notes = "Store any data here")
    //private PersonalSettings personalSettings = new PersonalSettings();

    @DiffIgnore
    @ApiModelProperty(value = "last online user date", notes = "This is sent by client using GET `/myaccount/setOnline` API call")
    private Date lastOnline;

    private List<UserAccountGroup> groups = new ArrayList<>();

    @ApiModelProperty("User language")
    private String language;

    @ApiModelProperty("List of roles including roles from group and personal group ")
    private List<String> effectiveRoles = new ArrayList<>();

    @ApiModelProperty(value = "Pattern for allowed IP address to authentificate")
    private String ipPattern = null;

    @ApiModelProperty(value = "Does Two step auth enabled")
    private boolean twoStepEnabled;

    @ApiModelProperty("List of domains in which user is invited")
    private String domain;

    @ApiModelProperty(value = "String which contains serialized JSON", notes = "Store any data here")
    private PersonalSettings personalSettings = new PersonalSettings();

    public static enum UserAccountStatus {
        STATUS_PENDING, STATUS_APPROVED, STATUS_DISABLED, STATUS_PENDING_NOPASSWORD;
    }
}
