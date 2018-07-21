/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.securetoken;

import io.swagger.annotations.ApiModel;

@ApiModel(" ")
public class GoogleOauthResponseToken {

    /**
     * {
     * "access_token":"1/fFAGRNJru1FTz70BzhT3Zg",
     * "expires_in":3920,
     * "token_type":"Bearer"
     * }
     */

    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;


    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
}
