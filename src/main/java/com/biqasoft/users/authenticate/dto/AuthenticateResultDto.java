/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate.dto;

import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: separate to DTO
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
@Data
public class AuthenticateResultDto {

    // TODO: separate to DTO
    private UserAccount userAccount;

    // is user authenticated
    private Boolean authenticated = false;

    // list of user ROLES
    private List<String> auths = new ArrayList<>();

    // current authenticated user domain
    private Domain domain;

}
