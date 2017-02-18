/*
 * Copyright 2016 the original author or authors.
 */

package com.biqasoft.users.authenticate;

import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 7/25/2016
 *         All Rights Reserved
 */
public class AuthHelper {

    /**
     * see {@link org.springframework.security.web.authentication.www.BasicAuthenticationFilter#extractAndDecodeHeader(String, HttpServletRequest)}
     *
     * @param token
     * @return
     */
    public static UserNameWithPassword processTokenHeaderToUserNameAndPassword(String token) {
        String decodedToken;
        try {
            if (token.startsWith("Basic ")){
                String[] strings = token.split("Basic ", 2);
                if (strings.length != 2){
                    throw new InvalidRequestException("Invalid basic authentication token");
                }

                if (!Objects.equals(strings[0], "")){
                    throw new InvalidRequestException("Invalid basic authentication token");
                }

                token = strings[1];
            }

            decodedToken = new String(Base64.decodeBase64(token.getBytes("UTF-8"))); // spring security bug in decode from base64 -> use tomcat. see tests
            int delim = decodedToken.indexOf(":");
            if (delim == -1) {
                throw new InvalidRequestException("Invalid basic authentication token");
            }

            String[] loginPlusPassword = {decodedToken.substring(0, delim), decodedToken.substring(delim + 1)};

            UserNameWithPassword userNameWithPassword = new UserNameWithPassword();
            userNameWithPassword.username = loginPlusPassword[0];
            userNameWithPassword.password = loginPlusPassword[1];
            return userNameWithPassword;
        } catch (Exception e) {
            throw new BiqaAuthenticationLocalizedException("auth.exception.empty_password");
        }

    }

}
