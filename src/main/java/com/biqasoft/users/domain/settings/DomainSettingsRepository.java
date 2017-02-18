/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.domain.settings;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.microservice.database.MainDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class DomainSettingsRepository {

    private final MongoOperations ops;
    private final CurrentUser currentUser;

    @Autowired
    public DomainSettingsRepository(@MainDatabase MongoOperations ops, CurrentUser currentUser) {
        this.ops = ops;
        this.currentUser = currentUser;
    }

    public DomainSettings findDomainSettingsById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id) ), DomainSettings.class);
    }

    public DomainSettings addDomainSettings(DomainSettings note) {
        ops.insert(note);
        return note;
    }

    public DomainSettings updateDomainSettings(DomainSettings domainSettings) {
        if (domainSettings.getId().equals(currentUser.getDomain().getDomain())) {
            return update(domainSettings);
        }
        return domainSettings;
    }

    public DomainSettings update(DomainSettings domainSettings) {
            ops.save(domainSettings);
        return domainSettings;
    }

    public DomainSettings findDomainSettingsCurrentUser() {
        return ops.findOne(Query.query(Criteria.where("id").is(currentUser.getDomain().getDomain())  ), DomainSettings.class);
    }

    public void delete(String id){
        DomainSettings domainSettings = new DomainSettings();
        domainSettings.setId(id);

        ops.remove(domainSettings);
    }

}
