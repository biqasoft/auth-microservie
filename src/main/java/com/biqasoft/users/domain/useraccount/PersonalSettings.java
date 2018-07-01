/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.javers.core.metamodel.annotation.Value;

//TODO: make as inner class of com.biqasoft.users.domain.useraccount.UserAccount and UserAccount of auth microservice
/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
 * All Rights Reserved
 */
@Value
@Data
public class PersonalSettings {

    private String dateFormat;
    private SettingsColor colors = new SettingsColor();

    @ApiModelProperty("Any data that browser or client want to store, such as some settings etc")
    private String data;

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
     * All Rights Reserved
     */
    @Deprecated(forRemoval = true)
    @Data
    @Value
    public static class SettingsColor {

        private boolean enable;
        private String mainColor;

    }

}
