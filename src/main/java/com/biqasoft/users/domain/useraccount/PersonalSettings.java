/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain.useraccount;

import io.swagger.annotations.ApiModelProperty;
import org.javers.core.metamodel.annotation.Value;

//TODO: make as inner class of com.biqasoft.users.domain.useraccount.UserAccount and UserAccount of auth microservice
/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
 * All Rights Reserved
 */
@Value
public class PersonalSettings {

    private String dateFormat;
    private SettingsColor colors = new SettingsColor();

    // TODO: move to some JSON mongodb blob to prevent additional object allocation; merge with data field
    private SettingsUI ui = new SettingsUI();

    @ApiModelProperty("Any data that browser or client want to store, such as some settings etc")
    private String data;


    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public SettingsColor getColors() {
        return colors;
    }

    public void setColors(SettingsColor colors) {
        this.colors = colors;
    }

    public SettingsUI getUi() {
        return ui;
    }

    public void setUi(SettingsUI ui) {
        this.ui = ui;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonalSettings that = (PersonalSettings) o;

        if (dateFormat != null ? !dateFormat.equals(that.dateFormat) : that.dateFormat != null) return false;
        if (colors != null ? !colors.equals(that.colors) : that.colors != null) return false;
        if (ui != null ? !ui.equals(that.ui) : that.ui != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = dateFormat != null ? dateFormat.hashCode() : 0;
        result = 31 * result + (colors != null ? colors.hashCode() : 0);
        result = 31 * result + (ui != null ? ui.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
     * All Rights Reserved
     */
    @Value
    public static class SettingsColor {

        private boolean enable;
        private String mainColor;

        public String getMainColor() {
            return mainColor;
        }

        public void setMainColor(String mainColor) {
            this.mainColor = mainColor;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SettingsColor that = (SettingsColor) o;

            if (enable != that.enable) return false;
            return mainColor != null ? mainColor.equals(that.mainColor) : that.mainColor == null;

        }

        @Override
        public int hashCode() {
            int result = (enable ? 1 : 0);
            result = 31 * result + (mainColor != null ? mainColor.hashCode() : 0);
            return result;
        }
    }

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
     * All Rights Reserved
     */
    @Value
    public static class SettingsTableUi {

        private boolean modern = true;

        public boolean isModern() {
            return modern;
        }

        public void setModern(boolean modern) {
            this.modern = modern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SettingsTableUi that = (SettingsTableUi) o;

            if (modern != that.modern) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (modern ? 1 : 0);
        }
    }

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 5/26/2016.
     * All Rights Reserved
     */
    @Value
    public static class SettingsUI {

        private SettingsTableUi table = new SettingsTableUi();
        private boolean modernCustomFields = true;


        public SettingsTableUi getTable() {
            return table;
        }

        public void setTable(SettingsTableUi table) {
            this.table = table;
        }

        public boolean isModernCustomFields() {
            return modernCustomFields;
        }

        public void setModernCustomFields(boolean modernCustomFields) {
            this.modernCustomFields = modernCustomFields;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SettingsUI that = (SettingsUI) o;

            if (modernCustomFields != that.modernCustomFields) return false;
            return table != null ? table.equals(that.table) : that.table == null;

        }

        @Override
        public int hashCode() {
            int result = table != null ? table.hashCode() : 0;
            result = 31 * result + (modernCustomFields ? 1 : 0);
            return result;
        }
    }
}
