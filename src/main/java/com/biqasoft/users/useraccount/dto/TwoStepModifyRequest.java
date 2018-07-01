package com.biqasoft.users.useraccount.dto;

import lombok.Data;

@Data
public class TwoStepModifyRequest {

    private boolean enabled;
    private String code;

}
