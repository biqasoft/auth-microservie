/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain;

import com.biqasoft.entity.core.CreatedInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is object for system use
 * user can not modify it, but can see through API
 * user cam modify {@link DomainSettings} class
 */
@Document
@ApiModel("Represent main domain info such as balance, limits, etc. Not contain settings etc")
public class Domain implements Serializable {

    @Id
    private String domain;

    @ApiModelProperty("main balance for domain")
    private double balance;

    @ApiModelProperty("additional balance wich user get from some sales etc")
    private double bonusBalance;

    @ApiModelProperty("balance currency")
    private String balanceCurrency = "RUB";

    @ApiModelProperty("affiliate program / partners sales")
    private String partnerID;

    @ApiModelProperty("which tarif of all domain")
    private String tariff;

    @ApiModelProperty("when tariff is expires")
    private Date tariffUntil;

    @ApiModelProperty("this domain is active - not blocked. NOTE: not active domains do not process for metrics and data source changes")
    private boolean active = true;

    private CreatedInfo createdInfo;


    @JsonIgnore
    private DatabaseCredentials mainDatabaseCredentials = null;


    @JsonIgnore
    public DatabaseCredentials getMainDatabaseCredentials() {
        return mainDatabaseCredentials;
    }

    @JsonProperty("mainDatabaseCredentials")
    public void setMainDatabaseCredentials(DatabaseCredentials mainDatabaseCredentials) {
        this.mainDatabaseCredentials = mainDatabaseCredentials;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBonusBalance() {
        return bonusBalance;
    }

    public void setBonusBalance(double bonusBalance) {
        this.bonusBalance = bonusBalance;
    }

    public String getBalanceCurrency() {
        return balanceCurrency;
    }

    public void setBalanceCurrency(String balanceCurrency) {
        this.balanceCurrency = balanceCurrency;
    }

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID) {
        this.partnerID = partnerID;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }


    public Date getTariffUntil() {
        return tariffUntil;
    }

    public void setTariffUntil(Date tariffUntil) {
        this.tariffUntil = tariffUntil;
    }

    public CreatedInfo getCreatedInfo() {
        return createdInfo;
    }

    public void setCreatedInfo(CreatedInfo createdInfo) {
        this.createdInfo = createdInfo;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Created by Nikita Bakaev, ya@nbakaev.ru on 4/17/2016.
     * All Rights Reserved
     */
    public static class DatabaseCredentials {

        private String username = null;
        private String password = null;

        private String tenant = null;

        private List<String> roles = new ArrayList<>();


        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
