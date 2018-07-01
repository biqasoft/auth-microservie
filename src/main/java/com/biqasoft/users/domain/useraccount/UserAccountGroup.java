/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount;


import com.biqasoft.entity.core.BaseClass;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/6/2016.
 * All Rights Reserved
 */
@Data
public class UserAccountGroup extends BaseClass {

    private List<String> grantedRoles = new ArrayList<>();

    private List<String> userAccountsIDs = new ArrayList<>();

    private boolean enabled = true;

}
