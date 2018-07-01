/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config;

import com.biqasoft.common.exceptions.InvalidRequestLocalizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/9/2016
 *         All Rights Reserved
 */
public class ThrowAuthExceptionHelper {

    private final static Logger logger = LoggerFactory.getLogger(ThrowAuthExceptionHelper.class);

    /**
     * Catched by
     *
     * @param messageId id of i18n property in resource folder
     */
    public static void throwExceptionBiqaAuthenticationLocalizedException(String messageId) throws BiqaAuthenticationLocalizedException {
        throw new BiqaAuthenticationLocalizedException(messageId);
    }

    public static InvalidRequestLocalizedException throwJustExceptionBiqaAuthenticationLocalizedException(String messageId) throws BiqaAuthenticationLocalizedException {
        return new InvalidRequestLocalizedException(messageId);
    }

    public static Mono<BiqaAuthenticationLocalizedException> throwErrorReactiveBiqaAuthenticationLocalizedException(String messageId) {
        return Mono.error(new InvalidRequestLocalizedException(messageId));
    }

    public static BiqaAuthenticationLocalizedException justErrorReactiveBiqaAuthenticationLocalizedException(String messageId) {
        return new BiqaAuthenticationLocalizedException(messageId);
    }

    public static Mono throwErrorReactiveBiqaLocalizedException(String messageId) {
        return Mono.error(new InvalidRequestLocalizedException(messageId));
    }

}