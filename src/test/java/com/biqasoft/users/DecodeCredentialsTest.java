/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users;

import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 1/10/2016.
 * All Rights Reserved
 */
@Test
public class DecodeCredentialsTest{

    @Test
    public void testTokenBase64Decode() {
        UserNameWithPassword userAuth = AuthHelper.processTokenHeaderToUserNameAndPassword("Basic T0FVVEgyXzddP3UzcioxbDh2eXR6ZnYvW3coOi5jeUB6Lzo6Om5raWUvOjo6OnhhJ3Y5eDsqezo6Ojo");
        assertNotNull(userAuth);
        assertEquals(userAuth.username, "OAUTH2_7]?u3r*1l8vytzfv/[w(");
        assertEquals(userAuth.password, ".cy@z/:::nkie/::::xa'v9x;*{::::");
    }

    @Test
    public void testTokenBiqaDecode() {
        UserNameWithPassword userAuth = AuthHelper.processTokenHeaderToUserNameAndPassword("Biqa eyJ1c2VybmFtZSI6InlhQG5iYWthZXYucnUiLCJwYXNzd29yZCI6IjEyMyIsInR3b1N0ZXBDb2RlIjoiNjg4NDM1In0=");
        assertNotNull(userAuth);
        assertEquals(userAuth.username, "ya@nbakaev.ru");
        assertEquals(userAuth.password, "123");
        assertEquals(userAuth.getTwoStepCode(), "688435");
    }

    @Test(expectedExceptions = BiqaAuthenticationLocalizedException.class, expectedExceptionsMessageRegExp = "auth.exception.empty_password")
    public void expectErrorUser() {
        AuthHelper.processTokenHeaderToUserNameAndPassword("");
    }

    @Test(expectedExceptions = BiqaAuthenticationLocalizedException.class, expectedExceptionsMessageRegExp = "auth.exception.empty_password")
    public void expectErrorUser2() {
        AuthHelper.processTokenHeaderToUserNameAndPassword("Biqa 123");
    }

    @Test(expectedExceptions = BiqaAuthenticationLocalizedException.class, expectedExceptionsMessageRegExp = "auth.exception.empty_password")
    public void expectErrorUser3() {
        AuthHelper.processTokenHeaderToUserNameAndPassword("Basic 123");
    }

}