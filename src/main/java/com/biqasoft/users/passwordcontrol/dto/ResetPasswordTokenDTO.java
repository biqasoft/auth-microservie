/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.passwordcontrol.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
public class ResetPasswordTokenDTO {

    private String id = new ObjectId().toString();

    private String email;
    private String randomString;
    private String password;
    private Date expireDate;

}
