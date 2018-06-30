/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount;


import com.biqasoft.entity.core.BaseClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/6/2016.
 * All Rights Reserved
 */
public class UserAccountGroup extends BaseClass {

    private List<String> grantedRoles = new ArrayList<>();

    private List<String> userAccountsIDs = new ArrayList<>();

    private boolean enabled = true;


    public List<String> getUserAccountsIDs() {
        return userAccountsIDs;
    }

    public void setUserAccountsIDs(List<String> userAccountsIDs) {
        this.userAccountsIDs = userAccountsIDs;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getGrantedRoles() {
        return grantedRoles;
    }

    public void setGrantedRoles(List<String> grantedRoles) {
        this.grantedRoles = grantedRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAccountGroup that = (UserAccountGroup) o;

        if (enabled != that.enabled) return false;
        if (grantedRoles != null ? !grantedRoles.equals(that.grantedRoles) : that.grantedRoles != null) return false;
        return userAccountsIDs != null ? userAccountsIDs.equals(that.userAccountsIDs) : that.userAccountsIDs == null;

    }

    @Override
    public int hashCode() {
        int result = grantedRoles != null ? grantedRoles.hashCode() : 0;
        result = 31 * result + (userAccountsIDs != null ? userAccountsIDs.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
