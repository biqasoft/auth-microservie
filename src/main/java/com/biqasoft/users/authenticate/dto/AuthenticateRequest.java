/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate.dto;

import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
@Data
public class AuthenticateRequest {

    private String username;
    private String password;
    private String ip;
    private String token;

}
