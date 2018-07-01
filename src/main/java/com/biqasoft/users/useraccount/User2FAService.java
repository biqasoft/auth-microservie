package com.biqasoft.users.useraccount;

import com.biqasoft.users.auth.CurrentUserCtx;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.generateBase32Secret;

/**
 * Created by ya on 3/14/2017.
 */
@Service
public class User2FAService {

    private static final Logger logger = LoggerFactory.getLogger(User2FAService.class);

    private final UserAccountRepository userAccountRepository;

    public User2FAService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Request to change user 2FA
     *
     * @param newMode should be 2FA enabled
     * @param code    - if 2FA is enabled and user want to disable it
     */
    public Mono<Void> tryToChange2FA(boolean newMode, String code, CurrentUserCtx ctx) {
        return userAccountRepository.findByUserId(ctx.getUserAccount().getId(), ctx).flatMap(byUserId -> {
            if (byUserId.isTwoStepActivated() != newMode) {
                if (newMode) {
                    // enable
                    if (isTwoStepCodeValidForUser(byUserId, code)) {
                        byUserId.setTwoStepActivated(true);
                        return userAccountRepository.unsafeUpdateUserAccount(byUserId).flatMap((k) -> Mono.empty());
                    } else {
                        ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.security.2step.code.invalid.confirmation");
                    }
                } else {
                    // disable
                    byUserId.setTwoStepActivated(false);
                    return userAccountRepository.unsafeUpdateUserAccount(byUserId).flatMap((k) -> Mono.empty());
                }
            }
            return Mono.empty();
        });
    }


    /**
     * This function check if 2FA token is valid for provided user
     *
     * @param userAccount user
     * @param code2FA     provided by user token
     * @return true if 2FA code is valid. false if token is wrong
     */
    public boolean isTwoStepCodeValidForUser(UserAccount userAccount, @NotNull String code2FA) {
        String currentValidCode;
        try {
            if (StringUtils.isEmpty(userAccount.getTwoStepCode())) {
                logger.info("Empty 2FA auth code for user {}", userAccount.getUsername());
                return false;
            }

            currentValidCode = TimeBasedOneTimePasswordUtil.generateCurrentNumber(userAccount.getTwoStepCode());
        } catch (GeneralSecurityException e) {
            logger.error("Error creating 2FA auth code", e);
            return false;
        }
        return currentValidCode.equals(code2FA);
    }


//    TODO: generify for any user generate, not only spring context
//    public SecondFactorResponseDTO generateSecret2FactorForUser(UserAccount account) {
//
//    }

    /**
     * @return DTO with secret code for user
     */
    public Mono<SecondFactorResponseDTO> generateSecret2FactorForUser(CurrentUserCtx ctx) {
        SecondFactorResponseDTO secondFactorResponseDTO = new SecondFactorResponseDTO();

        UserAccount currentUserObj = ctx.getUserAccount();
        String base32Secret = generateBase32Secret();

        // generate the QR code
        String imageUrl = TimeBasedOneTimePasswordUtil.qrImageUrl(currentUserObj.getUsername(), base32Secret);

        // we can use the code here and compare it against user input
        return userAccountRepository.findByUserId(ctx.getUserAccount().getId(), ctx).map(byUserId -> {
            byUserId.setTwoStepCode(base32Secret);
            byUserId.setTwoStepActivated(false);
            userAccountRepository.unsafeUpdateUserAccount(byUserId);

            secondFactorResponseDTO.setImage(imageUrl);
            return secondFactorResponseDTO;
        });
    }

    @Data
    static class SecondFactorResponseDTO {

        private String image;

    }

}
