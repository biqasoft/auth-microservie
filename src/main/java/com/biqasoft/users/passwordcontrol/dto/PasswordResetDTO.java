/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.passwordcontrol.dto;

import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 8/4/2016
 * All Rights Reserved
 */
@Data
public class PasswordResetDTO {

    private String userId;
    private String username;
    private String newPassword;
}
