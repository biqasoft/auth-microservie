/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount.dbo;

import com.biqasoft.entity.core.GlobalStoredBaseClass;
import com.biqasoft.users.domain.useraccount.PersonalSettings;
import com.biqasoft.users.domain.useraccount.UserAccountGroup;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("This is main user account object")
@Document("userAccount")
public class UserAccountDbo extends GlobalStoredBaseClass {

    @NotNull
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    @TextIndexed
    @ApiModelProperty("Username - login")
    private String username;

    @DiffIgnore
    private String password;

    @ApiModelProperty("telephone")
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

    @DiffIgnore
    private List<UserAccountOAuth2> oAuth2s = new ArrayList<>();

    private List<UserAccountGroup> groups = new ArrayList<>();

    @ApiModelProperty("User language")
    private String language;

    private String twoStepCode;

    private boolean twoStepActivated;

    @ApiModelProperty(value = "Pattern for allowed IP address to authentificate")
    private String ipPattern = null;

    public static enum UserAccountStatus {
        STATUS_PENDING, STATUS_APPROVED, STATUS_DISABLED, STATUS_PENDING_NOPASSWORD;
    }
}
