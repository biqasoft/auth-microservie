/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount.oauth2;

import com.biqasoft.entity.core.GlobalStoredBaseClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 10/9/2015
 * All Rights Reserved
 */
@Document
public class OAuth2Application extends GlobalStoredBaseClass {

    private String response_type = "code";
    private String redirect_uri;

    @JsonIgnore
    private String secretCode;

    private List<String> roles = new ArrayList<>();

    @ApiModelProperty(value = "If this is true, application will automatically give access to application in browser",
            notes = "Used for trusted partners and application")
    private boolean giveAccessWithoutPrompt = false;

    @ApiModelProperty("This is publically available application")
    private boolean publicApp = false;


    public boolean isPublicApp() {
        return publicApp;
    }

    public void setPublicApp(boolean publicApp) {
        this.publicApp = publicApp;
    }

    public boolean isGiveAccessWithoutPrompt() {
        return giveAccessWithoutPrompt;
    }

    public void setGiveAccessWithoutPrompt(boolean giveAccessWithoutPrompt) {
        this.giveAccessWithoutPrompt = giveAccessWithoutPrompt;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @JsonIgnore
    public String getSecretCode() {
        return secretCode;
    }

    @JsonProperty("secretCode")
    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }

    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    public String getResponse_type() {
        return response_type;
    }

    public void setResponse_type(String response_type) {
        this.response_type = response_type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OAuth2Application that = (OAuth2Application) o;

        if (giveAccessWithoutPrompt != that.giveAccessWithoutPrompt) return false;
        if (publicApp != that.publicApp) return false;
        if (response_type != null ? !response_type.equals(that.response_type) : that.response_type != null)
            return false;
        if (redirect_uri != null ? !redirect_uri.equals(that.redirect_uri) : that.redirect_uri != null) return false;
        return roles != null ? roles.equals(that.roles) : that.roles == null;

    }

    @Override
    public int hashCode() {
        int result = response_type != null ? response_type.hashCode() : 0;
        result = 31 * result + (redirect_uri != null ? redirect_uri.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (giveAccessWithoutPrompt ? 1 : 0);
        result = 31 * result + (publicApp ? 1 : 0);
        return result;
    }
}
