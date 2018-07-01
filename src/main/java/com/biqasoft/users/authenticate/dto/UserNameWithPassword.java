/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate.dto;

import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/25/2016
 *         All Rights Reserved
 */
@Data
public class UserNameWithPassword {
    public String username;
    public String password;
    public String twoStepCode;

}
