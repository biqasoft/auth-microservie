/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.config;

import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Global config object which is stored in main database for internal app configuration / state
 */
@Document
@ApiModel
public class SystemSettings implements Serializable {

    @Id
    private String id = new ObjectId().toString();

    private String systemOAuthApplicationId = null;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSystemOAuthApplicationId() {
        return systemOAuthApplicationId;
    }

    public void setSystemOAuthApplicationId(String systemOAuthApplicationId) {
        this.systemOAuthApplicationId = systemOAuthApplicationId;
    }
}
