/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.notifications;

import com.biqasoft.notifications.email.Email;
import com.biqasoft.notifications.email.EmailSenderProvider;
import com.biqasoft.users.passwordcontrol.dto.ResetPasswordTokenDTO;
import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Prepare email templates  with {@code https://github.com/jknack/handlebars.java}
 * and send with {@link EmailSenderProvider}
 */
@Service
public class EmailPrepareAndSendService {

    private final Handlebars handlebars;
    private final Map<String, Template> compiledTemplates;
    private final EmailSenderProvider emailSenderProvider;
    private String baseCloudUrl;
    private String biqaSupportEmail;
    private String biqaSupportUrl;
    private String senderEmail;
    private String systemName;

    @Autowired
    public EmailPrepareAndSendService(EmailSenderProvider emailSenderProvider,
                                      @Value("${biqa.urls.http.cloud}") String baseCloudUrl,
                                      @Value("${biqa.urls.support.email}") String biqaSupportEmail,
                                      @Value("${biqa.urls.http.support}") String biqaSupportUrl,
                                      @Value("${biqa.notification.email.sender.email}") String senderEmail,
                                      @Value("${biqa.notification.header.system}") String systemName
    ) {
        this.handlebars = new Handlebars();
        this.compiledTemplates = new HashMap<>();
        this.emailSenderProvider = emailSenderProvider;
        this.baseCloudUrl = baseCloudUrl;
        this.biqaSupportEmail = biqaSupportEmail;
        this.biqaSupportUrl = biqaSupportUrl;
        this.senderEmail = senderEmail;
        this.systemName = systemName;

        try {
            compiledTemplates.put("create_domain", handlebars.compile("templates/create_domain"));
            compiledTemplates.put("reset_password_text", handlebars.compile("templates/reset_password_text"));
            compiledTemplates.put("welcome_message_to_new_useraccount", handlebars.compile("templates/welcome_message_to_new_useraccount"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendWelcomeEmailWhenCreateNewDomain(UserAccountDbo user, String password) {
        Template template = compiledTemplates.get("create_domain");

        Map<String, Object> map = new HashMap<>();
        map.put("userName", user.getUsername());
        map.put("userPassword", password);
        addBaseParamsToMap(map);

        String emailText = processTemplateWithContext(template, createContextFromMap(map));
        Email email = new Email(senderEmail, user.getEmail(), emailText, "Добро пожаловать в " + systemName);

        emailSenderProvider.sendEmail(email);
    }

    public void sendWelcomeEmailWhenAddNewUserToDomain(UserAccountDbo user, String password) {
        Template template = compiledTemplates.get("welcome_message_to_new_useraccount");

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("password", password);
        addBaseParamsToMap(map);

        String emailText = processTemplateWithContext(template, createContextFromMap(map));
        Email email = new Email(senderEmail, user.getEmail(), emailText, "Вход в " + systemName);

        emailSenderProvider.sendEmail(email);
    }

    public void sendResetPassword(ResetPasswordTokenDTO resetPasswordTokenDao) {
        Template template = compiledTemplates.get("reset_password_text");

        Map<String, Object> map = new HashMap<>();

        String randomString = Base64.encodeBase64String(resetPasswordTokenDao.getRandomString().getBytes());

        map.put("resetPasswordTokenDao", resetPasswordTokenDao);
        map.put("randomString", randomString);
        addBaseParamsToMap(map);

        String emailText = processTemplateWithContext(template, createContextFromMap(map));
        Email email = new Email(senderEmail, resetPasswordTokenDao.getEmail(), emailText, "Восстановление пароля в " + systemName);

        emailSenderProvider.sendEmail(email);
    }


    private Context createContextFromMap(Map<String, Object> map) {
        Context context = Context
                .newBuilder(map)
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
        return context;
    }

    private String processTemplateWithContext(Template template, Context context) {
        String emailText;
        try {
            emailText = template.apply(context);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return emailText;
    }

    private void addBaseParamsToMap(Map<String, Object> map) {
        map.put("baseCloudUrl", baseCloudUrl);
        map.put("biqaSupportEmail", biqaSupportEmail);
        map.put("biqaSupportUrl", biqaSupportUrl);
    }

}
