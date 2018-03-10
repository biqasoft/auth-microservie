/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.oauth2.application;

import io.swagger.annotations.ApiModel;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 12/5/2015
 * All Rights Reserved
 */
@ApiModel("Common used DTO with one string field")
public class SampleDataResponse {

    private String data;

    public SampleDataResponse(String data) {
        this.data = data;
    }

    public SampleDataResponse() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
