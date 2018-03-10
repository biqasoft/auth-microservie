/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.microservice.database.MongoTenantHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class MongoTenantConfiguration {

    private final CurrentUser currentUser;
    private final MongoTenantHelper mongoTenantHelper;

    // this is connection to user databases with non admin roles
    private ConcurrentHashMap<String, MongoTemplate> allDomainsUsersAccountMap = new ConcurrentHashMap<>(30);

    @Autowired
    public MongoTenantConfiguration(CurrentUser currentUser, MongoTenantHelper mongoTenantHelper) {
        this.currentUser = currentUser;
        this.mongoTenantHelper = mongoTenantHelper;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MongoTemplate domainBasedTemplate() {
        String domain = currentUser.getDomain().getDomain();
        return mongoTenantHelper.domainDataBaseUnsafeGet(domain);
    }

    public ConcurrentHashMap<String, MongoTemplate> getAllDomainsUsersAccountMap() {
        return allDomainsUsersAccountMap;
    }
}
