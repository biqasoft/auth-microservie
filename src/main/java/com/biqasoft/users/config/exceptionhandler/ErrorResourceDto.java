/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config.exceptionhandler;

import lombok.Data;

@Data
public class ErrorResourceDto {

    private String code;
    private String message;

    private String englishErrorMessage = null;
    private String idErrorMessage = null;

    private String domain;

    public ErrorResourceDto(String code, String message) {
        this.code = code;
        this.message = message;
    }

}