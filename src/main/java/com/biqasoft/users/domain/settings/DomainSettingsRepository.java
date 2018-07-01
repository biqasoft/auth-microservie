/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.domain.settings;

import com.biqasoft.users.domain.DomainSettings;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Deprecated(forRemoval = true)
@Service
public class DomainSettingsRepository {

    private final MongoOperations ops;

    @Autowired
    public DomainSettingsRepository(@MainDatabase MongoOperations ops) {
        this.ops = ops;
    }

    public DomainSettings findDomainSettingsById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id) ), DomainSettings.class);
    }

    public DomainSettings addDomainSettings(DomainSettings note) {
        ops.insert(note);
        return note;
    }

    public DomainSettings updateDomainSettings(DomainSettings domainSettings, CurrentUserCtx ctx) {
        if (domainSettings.getId().equals(ctx.getDomain().getDomain())) {
            return update(domainSettings);
        }
        return domainSettings;
    }

    public DomainSettings update(DomainSettings domainSettings) {
            ops.save(domainSettings);
        return domainSettings;
    }

    public DomainSettings findDomainSettingsCurrentUser(CurrentUserCtx ctx) {
        return ops.findOne(Query.query(Criteria.where("id").is(ctx.getDomain().getDomain())  ), DomainSettings.class);
    }

    public void delete(String id){
        DomainSettings domainSettings = new DomainSettings();
        domainSettings.setId(id);

        ops.remove(domainSettings);
    }

}
