/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.passwordcontrol.dto;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 8/4/2016
 *         All Rights Reserved
 */
public class PasswordResetDTO {

    private String userId;
    private String username;
    private String newPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
