/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.users.domain;

import com.biqasoft.entity.core.objects.CustomField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.biqasoft.entity.constants.TOKEN_TYPES;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated(forRemoval = true)
@Document
@ApiModel
public class DomainSettings implements Serializable {

    @Id
    @ApiModelProperty("same id as domain (domainInCRM)")
    private String id;

    private String defaultIDSalesFunnelLG;
    private String defaultIDSalesFunnelLC;
    private String defaultIDSalesFunnelAM;

    private String defaultLeadGenMethodID;
    private String defaultLeadGenProjectID;

    private String defaultEmail;

    private String logoText;
    private String currency;

    @ApiModelProperty("When file is stored - Dropbox, default storage etc")
    private String defaultUploadStoreType = TOKEN_TYPES.DEFAULT_STORAGE;

    @ApiModelProperty("Default Storage ID in store. For example, if it is dropbox - it is login of account etc...")
    private String defaultUploadStoreID;

    @ApiModelProperty("Time zone offset in MINUTES")
    private int timeZoneOffset;

    @ApiModelProperty("If user do not complete task - allow done task")
    private boolean allowCompleteTaskWithoutCheckList = false;

    // key is name of biqaClass BiqaClassService#getNameForBiqaClass
    private Map<String, List<CustomField>> defaultCustomFields = new HashMap<>();

    /**
     * add ???
     * staticSegment
     * dynamicSegment
     */
//    private List<CustomField> customFieldsCustomer = new ArrayList<>();
//    private List<CustomField> customFieldsCompany = new ArrayList<>();
//    private List<CustomField> customFieldsDeals = new ArrayList<>();
//    private List<CustomField> customFieldsCosts = new ArrayList<>();
//    private List<CustomField> customFieldsOpportunity = new ArrayList<>();
//    private List<CustomField> customFieldsUserAccount = new ArrayList<>();
//    private List<CustomField> customFieldsTask = new ArrayList<>();
//    private List<CustomField> customFieldsTaskProject = new ArrayList<>();
//    private List<CustomField> customFieldsLeadGenMethod = new ArrayList<>();
//    private List<CustomField> customFieldsLeadGenProject = new ArrayList<>();

//    protected List<CustomField> customFields = new ArrayList<>();

    private String customerFolderId = null;

    @JsonIgnore
    public List<CustomField> getCustomFieldForClass(String className){
        List<CustomField> customFields = defaultCustomFields.get(className);
        if (customFields == null){
            customFields = new ArrayList<>();
            defaultCustomFields.put(className, customFields);
        }

        return customFields;
    }

    @JsonIgnore
    public void setCustomFieldForClass(String className,  List<CustomField> customFields){
        defaultCustomFields.put(className, customFields);
    }

    public String getCustomerFolderId() {
        return customerFolderId;
    }

    public void setCustomerFolderId(String customerFolderId) {
        this.customerFolderId = customerFolderId;
    }

    public boolean isAllowCompleteTaskWithoutCheckList() {
        return allowCompleteTaskWithoutCheckList;
    }

    public void setAllowCompleteTaskWithoutCheckList(boolean allowCompleteTaskWithoutCheckList) {
        this.allowCompleteTaskWithoutCheckList = allowCompleteTaskWithoutCheckList;
    }

    public String getDefaultUploadStoreType() {
        return defaultUploadStoreType;
    }

    public void setDefaultUploadStoreType(String defaultUploadStoreType) {
        this.defaultUploadStoreType = defaultUploadStoreType;
    }

    public String getDefaultUploadStoreID() {
        return defaultUploadStoreID;
    }

    public void setDefaultUploadStoreID(String defaultUploadStoreID) {
        this.defaultUploadStoreID = defaultUploadStoreID;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getDefaultIDSalesFunnelLG() {
        return defaultIDSalesFunnelLG;
    }

    public void setDefaultIDSalesFunnelLG(String defaultIDSalesFunnelLG) {
        this.defaultIDSalesFunnelLG = defaultIDSalesFunnelLG;
    }

    public String getDefaultIDSalesFunnelLC() {
        return defaultIDSalesFunnelLC;
    }

    public void setDefaultIDSalesFunnelLC(String defaultIDSalesFunnelLC) {
        this.defaultIDSalesFunnelLC = defaultIDSalesFunnelLC;
    }

    public String getDefaultIDSalesFunnelAM() {
        return defaultIDSalesFunnelAM;
    }

    public void setDefaultIDSalesFunnelAM(String defaultIDSalesFunnelAM) {
        this.defaultIDSalesFunnelAM = defaultIDSalesFunnelAM;
    }

    public String getDefaultLeadGenMethodID() {
        return defaultLeadGenMethodID;
    }

    public void setDefaultLeadGenMethodID(String defaultLeadGenMethodID) {
        this.defaultLeadGenMethodID = defaultLeadGenMethodID;
    }

    public String getDefaultLeadGenProjectID() {
        return defaultLeadGenProjectID;
    }

    public void setDefaultLeadGenProjectID(String defaultLeadGenProjectID) {
        this.defaultLeadGenProjectID = defaultLeadGenProjectID;
    }

    public String getDefaultEmail() {
        return defaultEmail;
    }

    public void setDefaultEmail(String defaultEmail) {
        this.defaultEmail = defaultEmail;
    }

    public String getLogoText() {
        return logoText;
    }

    public void setLogoText(String logoText) {
        this.logoText = logoText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainSettings that = (DomainSettings) o;

        if (timeZoneOffset != that.timeZoneOffset) return false;
        if (allowCompleteTaskWithoutCheckList != that.allowCompleteTaskWithoutCheckList) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (defaultIDSalesFunnelLG != null ? !defaultIDSalesFunnelLG.equals(that.defaultIDSalesFunnelLG) : that.defaultIDSalesFunnelLG != null)
            return false;
        if (defaultIDSalesFunnelLC != null ? !defaultIDSalesFunnelLC.equals(that.defaultIDSalesFunnelLC) : that.defaultIDSalesFunnelLC != null)
            return false;
        if (defaultIDSalesFunnelAM != null ? !defaultIDSalesFunnelAM.equals(that.defaultIDSalesFunnelAM) : that.defaultIDSalesFunnelAM != null)
            return false;
        if (defaultLeadGenMethodID != null ? !defaultLeadGenMethodID.equals(that.defaultLeadGenMethodID) : that.defaultLeadGenMethodID != null)
            return false;
        if (defaultLeadGenProjectID != null ? !defaultLeadGenProjectID.equals(that.defaultLeadGenProjectID) : that.defaultLeadGenProjectID != null)
            return false;
        if (defaultEmail != null ? !defaultEmail.equals(that.defaultEmail) : that.defaultEmail != null) return false;
        if (logoText != null ? !logoText.equals(that.logoText) : that.logoText != null) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (defaultUploadStoreType != null ? !defaultUploadStoreType.equals(that.defaultUploadStoreType) : that.defaultUploadStoreType != null)
            return false;
        if (defaultUploadStoreID != null ? !defaultUploadStoreID.equals(that.defaultUploadStoreID) : that.defaultUploadStoreID != null)
            return false;
        if (defaultCustomFields != null ? !defaultCustomFields.equals(that.defaultCustomFields) : that.defaultCustomFields != null)
            return false;
        return customerFolderId != null ? customerFolderId.equals(that.customerFolderId) : that.customerFolderId == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (defaultIDSalesFunnelLG != null ? defaultIDSalesFunnelLG.hashCode() : 0);
        result = 31 * result + (defaultIDSalesFunnelLC != null ? defaultIDSalesFunnelLC.hashCode() : 0);
        result = 31 * result + (defaultIDSalesFunnelAM != null ? defaultIDSalesFunnelAM.hashCode() : 0);
        result = 31 * result + (defaultLeadGenMethodID != null ? defaultLeadGenMethodID.hashCode() : 0);
        result = 31 * result + (defaultLeadGenProjectID != null ? defaultLeadGenProjectID.hashCode() : 0);
        result = 31 * result + (defaultEmail != null ? defaultEmail.hashCode() : 0);
        result = 31 * result + (logoText != null ? logoText.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (defaultUploadStoreType != null ? defaultUploadStoreType.hashCode() : 0);
        result = 31 * result + (defaultUploadStoreID != null ? defaultUploadStoreID.hashCode() : 0);
        result = 31 * result + timeZoneOffset;
        result = 31 * result + (allowCompleteTaskWithoutCheckList ? 1 : 0);
        result = 31 * result + (defaultCustomFields != null ? defaultCustomFields.hashCode() : 0);
        result = 31 * result + (customerFolderId != null ? customerFolderId.hashCode() : 0);
        return result;
    }

    public Map<String, List<CustomField>> getDefaultCustomFields() {
        return defaultCustomFields;
    }

    public void setDefaultCustomFields(Map<String, List<CustomField>> defaultCustomFields) {
        this.defaultCustomFields = defaultCustomFields;
    }

}
