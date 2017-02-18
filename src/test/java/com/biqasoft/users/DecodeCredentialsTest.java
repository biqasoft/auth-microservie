/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users;

import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
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
        UserNameWithPassword userAuth = AuthHelper.processTokenHeaderToUserNameAndPassword("T0FVVEgyXzddP3UzcioxbDh2eXR6ZnYvW3coOi5jeUB6Lzo6Om5raWUvOjo6OnhhJ3Y5eDsqezo6Ojo");
        assertNotNull(userAuth);
        assertEquals(userAuth.username, "OAUTH2_7]?u3r*1l8vytzfv/[w(");
        assertEquals(userAuth.password, ".cy@z/:::nkie/::::xa'v9x;*{::::");
    }

}