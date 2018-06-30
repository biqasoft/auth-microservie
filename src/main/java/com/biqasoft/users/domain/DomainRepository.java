/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.domain;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.users.auth.CurrentUserCtx;
import org.bson.Document;
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

import javax.annotation.Nullable;
import java.util.List;

@Service
public class DomainRepository {

    private final MongoOperations ops;
    private final MongoTenantHelper mongoTenantHelper;
    private static final Logger logger = LoggerFactory.getLogger(DomainRepository.class);
    private final RandomString domainNameRandomString;

    @Autowired
    public DomainRepository(@MainDatabase MongoOperations ops, MongoTenantHelper mongoTenantHelper,
                            @Value("${biqa.domain.default.length}") Integer defaultDomainLength) {
        this.ops = ops;
        this.mongoTenantHelper = mongoTenantHelper;
        this.domainNameRandomString  = new RandomString(defaultDomainLength, RandomString.Strategy.ONLY_ENGLISH_CHARS);
    }

    public Domain findDomainById(String domain) {
        return ops.findOne(Query.query(Criteria.where("domain").is(domain)), Domain.class);
    }

    public Domain findDomainCurrentUser(CurrentUserCtx ctx) {
        return ops.findOne(Query.query(Criteria.where("domain").is(ctx.getDomain().getDomain())), Domain.class);
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
    public Document deleteDomainUnsafe(String domain) {
        Domain domainInCRM = findDomainById(domain);
        if (domainInCRM == null) {
            throw new RuntimeException("No such domain");
        }

        // delete full database
        Document cmd = new Document();
        cmd.put("dropDatabase", 1);

        MongoTemplate template = mongoTenantHelper.domainDataBaseUnsafeGet(domain);

        Document result = template.getDb().runCommand(cmd);

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


    /**
     *
     * @param domain new domain ofr customization
     * @return new created domain
     */
    public Domain addDomain(@Nullable Domain domain) {
        if (domain == null){
            domain = new Domain();
        }

        // do not allow to add domain with the same name
        if (StringUtils.isEmpty(domain.getDomain()) || findDomainById(domain.getDomain()) != null){
            String domainName;

            // try to generate not used domain name
            while (true) {
                domainName = domainNameRandomString.nextString();
                if (findDomainById(domainName) == null) break;
            }
            domain.setDomain(domainName);
        }

        ops.insert(domain);
        return domain;
    }

    public Domain updateDomain(Domain note) {
        ops.save(note);
        return note;
    }

    public Domain updateDomainForCurrentUser(Domain domain, CurrentUserCtx ctx) {
        if (!domain.getDomain().equals(ctx.getDomain().getDomain())){
            ThrowExceptionHelper.throwExceptionInvalidRequest("Try to modify not own domain");
        }

        ops.save(domain);
        return domain;
    }

}
