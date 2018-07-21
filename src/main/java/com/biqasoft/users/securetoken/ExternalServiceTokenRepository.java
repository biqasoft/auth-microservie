/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.securetoken;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.microservice.database.MainReactiveDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExternalServiceTokenRepository {

    private final ReactiveMongoOperations mainDataBase;

    @Autowired
    public ExternalServiceTokenRepository(@MainReactiveDatabase ReactiveMongoOperations mainDataBase) {
        this.mainDataBase = mainDataBase;
    }

    @BiqaAddObject
    public Mono<ExternalServiceToken> addExternalServiceToken(CurrentUserCtx ctx, ExternalServiceToken token) {
        token.setDomain(ctx.getDomain().getDomain());
        token.setCreatedInfo(new CreatedInfo(LocalDateTime.now(), ctx.getUserAccount().getId()));
        return mainDataBase.insert(token);
    }

    public Flux<ExternalServiceToken> findAllStorageTokens(CurrentUserCtx ctx) {
        return findExternalServiceTokensByType(ctx,
                Lists.newArrayList(TOKEN_TYPES.DROPBOX, TOKEN_TYPES.DEFAULT_STORAGE, TOKEN_TYPES.GOOGLE_DRIVE, TOKEN_TYPES.WEBDAV, TOKEN_TYPES.S3_COMPATIBLE));
    }

    /**
     * this method is ONLY internal
     * if we call it, we should know that
     * in new token we have 'token'
     * and maybe refreshToken fields
     * because it will override in dataBase
     *
     * @param newToken token to force update
     * @return
     */
    public Mono<ExternalServiceToken> updateExternalServiceToken(CurrentUserCtx ctx, ExternalServiceToken newToken) {
        return findExternalServiceTokenByIdIgnoreExpired(ctx, newToken.getId())
                .switchIfEmpty(Mono.error(new BiqaAuthenticationLocalizedException("token not exists")))
                .flatMap(tokenWithSame -> mainDataBase.save(newToken));
    }

    /**
     * used when user want update some token info
     * meta data such as name
     *
     * @param newToken
     * @return
     */
    public Mono<ExternalServiceToken> updateExternalServiceTokenForUser(CurrentUserCtx ctx, ExternalServiceToken newToken) {
        return findExternalServiceTokenByIdIgnoreExpired(ctx, newToken.getId()).flatMap(tokenWithSame -> {
            // because we hide token - sensitive info to user in API
            // we should get it
            newToken.setRefreshToken(tokenWithSame.getRefreshToken());
            newToken.setToken(tokenWithSame.getToken());
            return mainDataBase.save(newToken);
        });
    }

    /**
     * @param id token id to delete
     * @return true if token exists, otherwise return false
     */
    public Mono<Boolean> deleteExternalServiceTokenById(CurrentUserCtx ctx, String id) {
        return findExternalServiceTokenByIdIgnoreExpired(ctx, id)
                .flatMap(token -> mainDataBase.remove(token).map(x -> true)).switchIfEmpty(Mono.just(false));
    }

    public Mono<ExternalServiceToken> findExternalServiceTokenById(CurrentUserCtx ctx, String id) {
        return mainDataBase.findOne(Query.query(Criteria.where("id").is(id).and("domain").is(ctx.getDomain().getDomain())), ExternalServiceToken.class);
    }

    /**
     * @param type {@link TOKEN_TYPES}
     * @return all tokens in current domain with type
     */
    public Flux<ExternalServiceToken> findExternalServiceTokensByType(CurrentUserCtx ctx, String type) {
        return mainDataBase.find(Query.query(Criteria
                .where("domain").is(ctx.getDomain().getDomain())
                .and("type").is(type)
        ), ExternalServiceToken.class);
    }

    public Flux<ExternalServiceToken> findExternalServiceTokensByType(CurrentUserCtx ctx, List<String> type) {
        return mainDataBase.find(Query.query(Criteria
                .where("domain").is(ctx.getDomain().getDomain())
                .and("type").in(type)
        ), ExternalServiceToken.class);
    }

    /**
     * Get token from db y id and type and do not try to update access code if expired
     *
     * @param id   token id
     * @param type token type
     * @return token
     */
    public Mono<ExternalServiceToken> findExternalServiceTokenByLoginAndTypeIgnoreExpired(CurrentUserCtx ctx, String id, String type) {
        return mainDataBase.findOne(Query.query(Criteria
                .where("login").is(id)
                .and("domain").is(ctx.getDomain().getDomain())
                .and("type").is(type)
        ), ExternalServiceToken.class);
    }

    /**
     * Get token from db y id and type and do not try to update access code if expired
     *
     * @param id token id
     * @return token
     */
    public Mono<ExternalServiceToken> findExternalServiceTokenByIdIgnoreExpired(CurrentUserCtx ctx, String id) {
        return mainDataBase.findOne(Query.query(Criteria
                .where("id").is(id)
                .and("domain").is(ctx.getDomain().getDomain())
        ), ExternalServiceToken.class);
    }

    /**
     * @return all tokens in current domain
     */
    public Flux<ExternalServiceToken> findAll(CurrentUserCtx ctx) {
        return mainDataBase.find(Query.query(Criteria.where("domain").is(ctx.getDomain().getDomain())), ExternalServiceToken.class);
    }

    /**
     * WARNING: only for internal USE, not user
     *
     * @return all tokens
     */
    public Flux<ExternalServiceToken> findAllTokensInAllDomains() {
        return mainDataBase.findAll(ExternalServiceToken.class);
    }

}
