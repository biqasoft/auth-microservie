/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.oauth2;

import com.biqasoft.entity.core.CreatedInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Value;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 10/9/2015
 * All Rights Reserved
 */
@Value
@DiffIgnore
@CompoundIndex(name = "authIndex", def = "{'userName': 1, 'accessToken': 1}")
public class UserAccountOAuth2 implements Serializable {

    @ApiModelProperty("Client OAuth application ID")
    private String clientApplicationID;

    @Indexed
    @ApiModelProperty("This is username, which must be send instead of `username` in basic auth")
    private String userName;

    @ApiModelProperty("This is token with which another server will make authenticating instead of `password` in basic auth ")
    private String accessToken;

    @ApiModelProperty("this is access code which will be send to another server and this server will obtain code to `accessToken`")
    private String accessCode;

    private Date expire;

    @ApiModelProperty("This is OAuth2 Scope")
    private List<String> roles = new ArrayList<>();

    @ApiModelProperty("Does this token enabled or not")
    private boolean enabled;

    private CreatedInfo createdInfo;


    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    @JsonIgnore
    public String getRolesCSV() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = this.roles.iterator(); iter.hasNext(); ) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientApplicationID() {
        return clientApplicationID;
    }

    public void setClientApplicationID(String clientApplicationID) {
        this.clientApplicationID = clientApplicationID;
    }

    public CreatedInfo getCreatedInfo() {
        return createdInfo;
    }

    public void setCreatedInfo(CreatedInfo createdInfo) {
        this.createdInfo = createdInfo;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAccountOAuth2 that = (UserAccountOAuth2) o;

        if (enabled != that.enabled) return false;
        if (clientApplicationID != null ? !clientApplicationID.equals(that.clientApplicationID) : that.clientApplicationID != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (expire != null ? !expire.equals(that.expire) : that.expire != null) return false;
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) return false;
        return createdInfo != null ? createdInfo.equals(that.createdInfo) : that.createdInfo == null;

    }

    @Override
    public int hashCode() {
        int result = clientApplicationID != null ? clientApplicationID.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (expire != null ? expire.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (createdInfo != null ? createdInfo.hashCode() : 0);
        return result;
    }
}
