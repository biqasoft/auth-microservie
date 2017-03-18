/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate.dto;

import java.io.Serializable;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/22/2016
 *         All Rights Reserved
 */
public class AuthenticateRequest implements Serializable {

    private String username;
    private String password;
    private String ip;
    private String token;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
