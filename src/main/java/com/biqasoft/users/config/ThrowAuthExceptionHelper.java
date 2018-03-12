/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/9/2016
 *         All Rights Reserved
 */
public class ThrowAuthExceptionHelper {

    private final static Logger logger = LoggerFactory.getLogger(ThrowAuthExceptionHelper.class);

    /**
     * Catched by {@link com.biqasoft.gateway.configs.exceptionhandler.MyExceptionHandler}
     *
     * @param messageId id of i18n property in resource folder
     */
    public static void throwExceptionBiqaAuthenticationLocalizedException(String messageId) throws BiqaAuthenticationLocalizedException {
        throw new BiqaAuthenticationLocalizedException(messageId);
    }

    public static Mono<BiqaAuthenticationLocalizedException> throwErrorReactiveBiqaAuthenticationLocalizedException(String messageId) {
        return Mono.error(new BiqaAuthenticationLocalizedException(messageId));
    }

}