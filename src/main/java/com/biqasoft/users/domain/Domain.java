/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain;

import com.biqasoft.entity.core.CreatedInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This is object for system use
 * user can not modify it, but can see through API
 * user cam modify {@link DomainSettings} class
 */
@Data
@Document
@ApiModel("Represent main domain info such as balance, limits, etc. Not contain settings etc")
public class Domain {

    @Id
    private String domain;

    @ApiModelProperty("this domain is active - not blocked. NOTE: not active domains do not process for metrics and data source changes")
    private boolean active = true;

    private CreatedInfo createdInfo;

    @JsonIgnore
    private DatabaseCredentials mainDatabaseCredentials = null;

    @JsonIgnore
    public DatabaseCredentials getMainDatabaseCredentials() {
        return mainDatabaseCredentials;
    }

    @JsonProperty("mainDatabaseCredentials")
    public void setMainDatabaseCredentials(DatabaseCredentials mainDatabaseCredentials) {
        this.mainDatabaseCredentials = mainDatabaseCredentials;
    }

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 4/17/2016.
     * All Rights Reserved
     */
    @Data
    public static class DatabaseCredentials {

        private String username = null;
        private String password = null;

        private String tenant = null;

        private List<String> roles = new ArrayList<>();

    }
}
