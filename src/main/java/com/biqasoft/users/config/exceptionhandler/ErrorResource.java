/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.config.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResource {
    private String code;
    private String message;

    private String englishErrorMessage = null;
    private String idErrorMessage = null;

    private String domain;

    public ErrorResource() {
    }

    public ErrorResource(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEnglishErrorMessage() {
        return englishErrorMessage;
    }

    public void setEnglishErrorMessage(String englishErrorMessage) {
        this.englishErrorMessage = englishErrorMessage;
    }

    public String getIdErrorMessage() {
        return idErrorMessage;
    }

    public void setIdErrorMessage(String idErrorMessage) {
        this.idErrorMessage = idErrorMessage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}