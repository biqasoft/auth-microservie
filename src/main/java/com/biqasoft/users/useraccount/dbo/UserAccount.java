/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount.dbo;

import com.biqasoft.entity.core.GlobalStoredBaseClass;
import com.biqasoft.users.domain.useraccount.PersonalSettings;
import com.biqasoft.users.domain.useraccount.UserAccountGroup;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("This is main user account object")
public class UserAccount extends GlobalStoredBaseClass {

    @NotNull
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    @TextIndexed
    @ApiModelProperty("Username - login")
    private String username;

    @DiffIgnore
    @JsonIgnore
    private String password;

    @ApiModelProperty("telephone")
//    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    @Indexed(dropDups = true, unique = true, sparse = true, name = "telephone")
    private String telephone;

    // just some user defined variables
    @TextIndexed
    private String firstname;

    @TextIndexed
    private String lastname;

    @TextIndexed
    private String patronymic;

    @TextIndexed
    private String position;

    @NotNull
    @ApiModelProperty(value = "user email", notes = " by default this is username")
    @TextIndexed
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

    @ApiModelProperty(value = "String which contains serialized JSON", notes = "Store any data here")
    private PersonalSettings personalSettings = new PersonalSettings();

    @DiffIgnore
    @ApiModelProperty(value = "last online user date", notes = "This is sent by client using GET `/myaccount/setOnline` API call")
    private Date lastOnline;

    @JsonIgnore
    @DiffIgnore
    private List<UserAccountOAuth2> oAuth2s = new ArrayList<>();

    private List<UserAccountGroup> groups = new ArrayList<>();

    @ApiModelProperty("User language")
    private String language;

    @ApiModelProperty("List of roles including roles from group and personal group ")
    private List<String> effectiveRoles = new ArrayList<>();

    @ApiModelProperty("List of domains in which user is invited")
    private List<String> domains = new ArrayList<>();

    private String twoStepCode;

    private boolean twoStepActivated;

    @ApiModelProperty(value = "Pattern for allowed IP address to authentificate")
    private String ipPattern = null;

    @JsonProperty("effectiveRoles")
    public List<String> getEffectiveRoles() {
        List<String> effectiveRoles = new ArrayList<>();

        if (!CollectionUtils.isEmpty(this.getGroups())) {
            for (UserAccountGroup group : this.getGroups()) {

                if (!group.isEnabled()) {
                    continue;
                }

                if (CollectionUtils.isEmpty(group.getGrantedRoles())) {
                    continue;
                }

                effectiveRoles.addAll(group.getGrantedRoles());
            }
        }

        if (!CollectionUtils.isEmpty(this.getRoles())) {
            effectiveRoles.addAll(this.getRoles());
        }

        return effectiveRoles;
    }

    @JsonIgnore
    public List<UserAccountOAuth2> getoAuth2s() {
        return oAuth2s;
    }

    @JsonProperty("oAuth2s")
    public void setoAuth2s(List<UserAccountOAuth2> oAuth2s) {
        this.oAuth2s = oAuth2s;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public static enum UserAccountStatus {
        STATUS_PENDING, STATUS_APPROVED, STATUS_DISABLED, STATUS_PENDING_NOPASSWORD;
    }
}
