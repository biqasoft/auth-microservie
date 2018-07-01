/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.users.passwordcontrol;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.passwordcontrol.dto.PasswordEncodeRequest;
import com.biqasoft.users.passwordcontrol.dto.PasswordResetDTO;
import com.biqasoft.users.passwordcontrol.dto.ResetPasswordTokenDTO;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PasswordResetRepository {

    private final MongoOperations ops;
    private final PasswordEncoder encoder;
    private final UserAccountRepository userAccountRepository;
    private final RandomString randomString;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final int resetTokenTtl;

    @Autowired
    public PasswordResetRepository(@MainDatabase MongoOperations ops, PasswordEncoder encoder, UserAccountRepository userAccountRepository,
                                   @Value("${biqa.auth.password.default.length}") Integer defaultPasswordLength, EmailPrepareAndSendService emailPrepareAndSendService,
                                   @Value("${biqa.auth.password.reset.default.ttl}") Integer resetTokenTtl) {
        this.ops = ops;
        this.resetTokenTtl = resetTokenTtl;
        this.encoder = encoder;
        this.userAccountRepository = userAccountRepository;

        randomString = new RandomString(defaultPasswordLength, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS);
        this.emailPrepareAndSendService = emailPrepareAndSendService;
    }

    /**
     *
     * @param resetPasswordTokenDao user submitted token
     * @return true if reset token is valid
     */
    public boolean validateResetPasswordToken(ResetPasswordTokenDTO resetPasswordTokenDao) {
        boolean valid = false;
        ResetPasswordTokenDTO token = getResetPasswordTokenDao(resetPasswordTokenDao);

        if (token != null) {
            if (resetTokenTtl == 0) {
                valid = true;
            } else {
                // check if token is not expired
                Date currentDate = new Date();
                if (token.getExpireDate() != null && token.getExpireDate().after(currentDate)) {
                    valid = true;
                }
            }
        }
        return valid;
    }

    public ResetPasswordTokenDTO getResetPasswordTokenDao(ResetPasswordTokenDTO resetPasswordTokenDao) {
        return ops.findOne(Query.query(Criteria
                .where("id").is(resetPasswordTokenDao.getId())
                .and("email").is(resetPasswordTokenDao.getEmail())
                .and("randomString").is(resetPasswordTokenDao.getRandomString())
        ), ResetPasswordTokenDTO.class);
    }

    public void resetUserPasswordBySecretToken(ResetPasswordTokenDTO resetPasswordTokenDao) {
        UserAccount userAccount = userAccountRepository.findByUsernameOrOAuthToken(resetPasswordTokenDao.getEmail()).block();
        userAccount.setPassword(encoder.encode(resetPasswordTokenDao.getPassword()));
        ops.save(userAccount);
        ops.remove(resetPasswordTokenDao);
    }

    public void addPasswordTokenDao(ResetPasswordTokenDTO resetPasswordTokenDao) {
        resetPasswordTokenDao.setId(new ObjectId().toString());
        resetPasswordTokenDao.setRandomString(randomString.nextString());

        // add expired date
        Date date = new Date(new Date().getTime() + resetTokenTtl);
        resetPasswordTokenDao.setExpireDate(date);

        ops.insert(resetPasswordTokenDao);
        emailPrepareAndSendService.sendResetPassword(resetPasswordTokenDao);
    }

    public PasswordResetDTO unsafeResetPassword(UserAccount userPosted, CurrentUserCtx ctx) {
        String password = randomString.nextString();

        PasswordEncodeRequest passwordEncodeRequest = new PasswordEncodeRequest();
        passwordEncodeRequest.setPassword(password);
        userPosted.setPassword(encoder.encode(passwordEncodeRequest.getPassword()));

        UserAccount oldUserAccount = userAccountRepository.findByUserId(userPosted.getId(), ctx).block();

        if (!ctx.getDomain().getDomain().equals(oldUserAccount.getDomain())) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("ACCESS DENY");
        }
        oldUserAccount.setPassword(userPosted.getPassword());
        userAccountRepository.unsafeUpdateUserAccount(oldUserAccount);

        PasswordResetDTO response = new PasswordResetDTO();
        response.setNewPassword(password);
        response.setUserId(oldUserAccount.getId());
        response.setUsername(oldUserAccount.getUsername());

        return response;
    }

}