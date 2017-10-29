package com.biqasoft.users.useraccount;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.springframework.stereotype.Service;

import static com.biqasoft.users.authenticate.chain.UserAuthChecks.isTwoStepCodeValidForUser;
import static com.j256.twofactorauth.TimeBasedOneTimePasswordUtil.generateBase32Secret;

/**
 * Created by ya on 3/14/2017.
 */
@Service
public class UserSecondFactorService {

    private final UserAccountRepository userAccountRepository;
    private final CurrentUser currentUser;

    public UserSecondFactorService(UserAccountRepository userAccountRepository, CurrentUser currentUser) {
        this.userAccountRepository = userAccountRepository;
        this.currentUser = currentUser;
    }

    /**
     * Request to change user 2FA
     * @param newMode should be 2FA enabled
     * @param code - if 2FA is enabled and user want to disable it
     */
    public void tryToChange2FactorMode(boolean newMode, String code) {
        UserAccount byUserId = userAccountRepository.findByUserId(currentUser.getCurrentUser().getId());

        if (byUserId.isTwoStepActivated() != newMode) {
            if (newMode) {
                // enable
                if (isTwoStepCodeValidForUser(byUserId, code)) {
                    byUserId.setTwoStepActivated(newMode);
                    userAccountRepository.unsafeUpdateUserAccount(byUserId);
                } else {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.security.2step.code.invalid.confirmation");
                }
            } else {
                // disable
                byUserId.setTwoStepActivated(newMode);
                userAccountRepository.unsafeUpdateUserAccount(byUserId);
            }
        }
    }

//    TODO: generify for any user generate, not only spring context
//    public SecondFactorResponseDTO generateSecret2FactorForUser(UserAccount account) {
//
//    }

    /**
     * @return DTO with secret code for user
     */
    public SecondFactorResponseDTO generateSecret2FactorForUser() {
        SecondFactorResponseDTO secondFactorResponseDTO = new SecondFactorResponseDTO();

        com.biqasoft.entity.core.useraccount.UserAccount currentUserObj = currentUser.getCurrentUser();
        String base32Secret = generateBase32Secret();

        // generate the QR code
        String imageUrl = TimeBasedOneTimePasswordUtil.qrImageUrl(currentUserObj.getUsername(), base32Secret);

        // we can use the code here and compare it against user input
        UserAccount byUserId = userAccountRepository.findByUserId(currentUser.getCurrentUser().getId());
        byUserId.setTwoStepCode(base32Secret);
        byUserId.setTwoStepActivated(false);
        userAccountRepository.unsafeUpdateUserAccount(byUserId);

        secondFactorResponseDTO.setImage(imageUrl);
        return secondFactorResponseDTO;
    }

    static class SecondFactorResponseDTO {

        private String image;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

}
