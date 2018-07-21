/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.useraccount.dto;


import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 8/7/2016
 *         All Rights Reserved
 */
@Data
public class CreatedUser {

    private UserAccountDbo userAccount;

    // do not add @JsonIgnore
    private String password;
    private String domain;

}
