/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.oauth2.dto;

import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/25/2016
 *         All Rights Reserved
 */
@Data
public class CreateTokenResponse {
    public String username;
    public String password;

}
