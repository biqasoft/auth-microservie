/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 1/10/2016.
 * All Rights Reserved
 */
@SpringBootTest(classes = StartApplication.class)
@WebAppConfiguration
@Test
@ActiveProfiles({"development", "test"})
public class StartApplicationTest extends AbstractTestNGSpringContextTests {

    @Test
    public void contextLoads() {
    }


}