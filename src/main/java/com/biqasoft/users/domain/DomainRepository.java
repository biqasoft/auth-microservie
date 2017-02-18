/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.domain;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.microservice.database.MainDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DomainRepository {

    private final MongoOperations ops;
    private final CurrentUser currentUser;
    private final MongoTenantHelper mongoTenantHelper;
    private static final Logger logger = LoggerFactory.getLogger(DomainRepository.class);
    private final RandomString domainNameRandomString;

    @Autowired
    public DomainRepository(@MainDatabase MongoOperations ops, CurrentUser currentUser, MongoTenantHelper mongoTenantHelper,
                            @Value("${biqa.domain.default.length}") Integer defaultDomainLength) {
        this.ops = ops;
        this.currentUser = currentUser;
        this.mongoTenantHelper = mongoTenantHelper;
        this.domainNameRandomString  = new RandomString(defaultDomainLength, RandomString.Strategy.ONLY_ENGLISH_CHARS);
    }

    public Domain findDomainById(String domain) {
        return ops.findOne(Query.query(Criteria.where("domain").is(domain)), Domain.class);
    }

    public Domain findDomainCurrentUser() {
        return ops.findOne(Query.query(Criteria.where("domain").is(currentUser.getDomain().getDomain())), Domain.class);
    }

    /**
     * this is internal function
     *
     * @return
     */
    public List<Domain> findAllInDomainsUnsafe() {
        return ops.findAll(Domain.class);
    }

    /**
     * execute  { dropDatabase: 1 }
     * https://docs.mongodb.org/v3.0/reference/command/dropDatabase/#dbcmd.dropDatabase
     *
     * @param domain
     * @return
     */
    public DBObject deleteDomainUnsafe(String domain) {
        Domain domainInCRM = findDomainById(domain);
        if (domainInCRM == null) {
            throw new RuntimeException("No such domain");
        }

        // delete full database
        DBObject cmd = new BasicDBObject();
        cmd.put("dropDatabase", 1);

        MongoTemplate template = mongoTenantHelper.domainDataBaseUnsafeGet(domain);

        CommandResult result = template.getDb().command(cmd);

        // delete domainCrmObject
        ops.remove(domainInCRM);

        // delete domainSettings in main database
        Criteria criteria = new Criteria("id").is(domain);
        Query query = new Query(criteria);
        ops.remove(query, DomainSettings.class);

//        if (blockUsers){
//        }
        logger.info("Deleting domain {}", domain);

        return result;
    }


    public Domain addDomain(Domain note) {
        // do not allow to add domain with the same name
        if (StringUtils.isEmpty(note.getDomain()) || findDomainById(note.getDomain()) != null){
            String domainName;

            // try to generate not used domain name
            while (true) {
                domainName = domainNameRandomString.nextString();
                if (findDomainById(domainName) == null) break;
            }
            note.setDomain(domainName);
        }

        ops.insert(note);
        return note;
    }

    public Domain updateDomain(Domain note) {
        ops.save(note);
        return note;
    }

    public Domain updateDomainForCurrentUser(Domain domain) {
        if (!domain.getDomain().equals(currentUser.getDomain().getDomain())){
            ThrowExceptionHelper.throwExceptionInvalidRequest("Try to modify not own domain");
        }

        ops.save(domain);
        return domain;
    }

}
