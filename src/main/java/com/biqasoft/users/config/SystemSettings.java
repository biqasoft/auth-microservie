/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.config;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Global config object which is stored in main database for internal app configuration / state
 */
@Document
@ApiModel
@Data
public class SystemSettings {

    @Id
    private String id = new ObjectId().toString();

    private String systemOAuthApplicationId = null;

}
