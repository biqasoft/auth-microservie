/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.useraccount;

import com.biqasoft.entity.core.GlobalStoredBaseClass;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.users.oauth2.UserAccountOAuth2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@ApiModel("This is main user account object")
public class UserAccount extends GlobalStoredBaseClass {

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


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

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

    public List<UserAccountGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserAccountGroup> groups) {
        this.groups = groups;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @JsonIgnore
    public List<UserAccountOAuth2> getoAuth2s() {
        return oAuth2s;
    }

    @JsonProperty("oAuth2s")
    public void setoAuth2s(List<UserAccountOAuth2> oAuth2s) {
        this.oAuth2s = oAuth2s;
    }

    @ApiModelProperty(value = "Pattern for allowed IP address to authentificate")
    private String ipPattern = null;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getIpPattern() {
        return ipPattern;
    }

    public void setIpPattern(String ipPattern) {
        this.ipPattern = ipPattern;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Date lastOnline) {
        this.lastOnline = lastOnline;
    }

    public PersonalSettings getPersonalSettings() {
        return personalSettings;
    }

    public void setPersonalSettings(PersonalSettings personalSettings) {
        this.personalSettings = personalSettings;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @JsonIgnore
    public String getRolesCSV() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = this.getEffectiveRoles().iterator(); iter.hasNext(); ) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAccount that = (UserAccount) o;

        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (firstname != null ? !firstname.equals(that.firstname) : that.firstname != null) return false;
        if (lastname != null ? !lastname.equals(that.lastname) : that.lastname != null) return false;
        if (patronymic != null ? !patronymic.equals(that.patronymic) : that.patronymic != null) return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (enabled != null ? !enabled.equals(that.enabled) : that.enabled != null) return false;
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) return false;
        if (personalSettings != null ? !personalSettings.equals(that.personalSettings) : that.personalSettings != null) return false;
        if (lastOnline != null ? !lastOnline.equals(that.lastOnline) : that.lastOnline != null) return false;
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (effectiveRoles != null ? !effectiveRoles.equals(that.effectiveRoles) : that.effectiveRoles != null) return false;
        return ipPattern != null ? ipPattern.equals(that.ipPattern) : that.ipPattern == null;

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + (patronymic != null ? patronymic.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (personalSettings != null ? personalSettings.hashCode() : 0);
        result = 31 * result + (lastOnline != null ? lastOnline.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (effectiveRoles != null ? effectiveRoles.hashCode() : 0);
        result = 31 * result + (ipPattern != null ? ipPattern.hashCode() : 0);
        return result;
    }

    public static enum UserAccountStatus {
        STATUS_PENDING, STATUS_APPROVED, STATUS_DISABLED, STATUS_PENDING_NOPASSWORD;
    }
}
