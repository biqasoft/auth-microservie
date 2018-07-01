/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.oauth2.application;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 12/5/2015
 * All Rights Reserved
 */
@Data
@ApiModel("Common used DTO with one string field")
public class SampleDataResponseDto {

    private String data;

}
