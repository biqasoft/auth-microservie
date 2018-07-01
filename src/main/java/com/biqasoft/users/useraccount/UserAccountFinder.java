package com.biqasoft.users.useraccount;

import com.biqasoft.audit.object.BaseClassFinder;
import com.biqasoft.entity.core.BaseClass;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * Created by Nikita on 9/13/2016.
 */
@Component
public class UserAccountFinder implements BaseClassFinder {

    private final MongoOperations ops;
    private final String maindb;

    @Autowired
    public UserAccountFinder(@MainDatabase MongoOperations ops, @Value("${db.database.main.name}") String maindb) {
        this.ops = ops;
        this.maindb = maindb;
    }

    @Override
    public boolean canFind(Class<?> classToFind, String database) {
        return classToFind.equals(UserAccount.class);
    }

    @Override
    public <T extends BaseClass> T findClass(T objectToFind, String dbName) {
        Criteria securedCriteria = new Criteria();
        securedCriteria.and("id").is(objectToFind.getId());
        securedCriteria.and("domain").is(dbName);

        Query securedQuery = new Query(securedCriteria);
        return (T)ops.findOne(securedQuery, objectToFind.getClass());
    }

    @Override
    public <T extends BaseClass> String forceChangeDatabaseForJavers(T objectToFind, String database) {
        if (objectToFind instanceof UserAccount){
            return maindb;
        }
        return null;
    }
}
