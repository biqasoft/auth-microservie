/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/25/2016
 *         All Rights Reserved
 */
public class AuthHelper {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * see {@link org.springframework.security.web.authentication.www.BasicAuthenticationFilter#extractAndDecodeHeader(String, HttpServletRequest)}
     *
     * @param token HTTP Authorization header value
     * @return extracted data from header
     */
    public static UserNameWithPassword processTokenHeaderToUserNameAndPassword(String token) {
        UserNameWithPassword result = null;

        if (token.startsWith("Basic ")) {
            result = tryExtractBasicAuth(token);
        } else if (token.startsWith("Biqa ")) {
            result = tryExtractBiqaAuth(token);
        }

        if (result == null) {
            throw new BiqaAuthenticationLocalizedException("auth.exception.empty_password");
        } else {
            return result;
        }

    }

    /**
     *
     * @param token HTTP Authorization header value
     * @return extracted data from header, or null if can not process header, or error happened
     */
    private static UserNameWithPassword tryExtractBiqaAuth(String token) {
        String decodedToken;
        try {
            token = token.replace("Biqa ", "");// / spring security bug in decode from base64 -> use tomcat. see tests
            decodedToken = new String(Base64.decodeBase64(token.getBytes("UTF-8")));
            return objectMapper.readValue(decodedToken, UserNameWithPassword.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     *
     * @param token HTTP Authorization header value
     * @return extracted data from header, or null if can not process header, or error happened
     */
    private static UserNameWithPassword tryExtractBasicAuth(String token) {
        String decodedToken;
        try {
            String[] strings = token.split("Basic ", 2);
            if (strings.length != 2) {
                return null;
            }

            if (!Objects.equals(strings[0], "")) {
                return null;
            }

            token = strings[1];

            decodedToken = new String(Base64.decodeBase64(token.getBytes("UTF-8"))); // spring security bug in decode from base64 -> use tomcat. see tests
            int delim = decodedToken.indexOf(":");
            if (delim == -1) {
                return null;
            }

            String[] loginPlusPassword = {decodedToken.substring(0, delim), decodedToken.substring(delim + 1)};

            UserNameWithPassword userNameWithPassword = new UserNameWithPassword();
            userNameWithPassword.username = loginPlusPassword[0];
            userNameWithPassword.password = loginPlusPassword[1];
            return userNameWithPassword;
        } catch (Exception e) {
            return null;
        }
    }

}
