/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import com.biqasoft.common.exceptions.dto.ErrorResource;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/28/2016
 *         All Rights Reserved
 */
public class BiqaAuthenticationLocalizedException extends AuthenticationException {

    private ErrorResource errorResource;

    public BiqaAuthenticationLocalizedException(String message) {
        super(message);
    }

}

