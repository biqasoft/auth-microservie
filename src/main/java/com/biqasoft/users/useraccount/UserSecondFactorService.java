package com.biqasoft.users.useraccount;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.springframework.stereotype.Service;

import static com.biqasoft.users.authenticate.RequestAuthenticateService.isTwoStepCodeValidForuser;
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

    public void modifyUserTwoStep(boolean newMode, String code) {
        UserAccount byUserId = userAccountRepository.findByUserId(currentUser.getCurrentUser().getId());

        if (byUserId.isTwoStepActivated() != newMode) {
            if (newMode) {
                // enable
                if (isTwoStepCodeValidForuser(byUserId, code)) {
                    byUserId.setTwoStepActivated(newMode);
                    userAccountRepository.unsafeUpdateUserAccount(byUserId);
                }else{
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("useraccount.security.2step.code.invalid.confirmation");
                }
            } else {
                // disable
                byUserId.setTwoStepActivated(newMode);
                userAccountRepository.unsafeUpdateUserAccount(byUserId);
            }
        }
    }

    public SecondFactorResponse processRequest() {
        SecondFactorResponse secondFactorResponse = new SecondFactorResponse();

        com.biqasoft.entity.core.useraccount.UserAccount currentUserObj = currentUser.getCurrentUser();
        String base32Secret = generateBase32Secret();

        // generate the QR code
        String imageUrl = TimeBasedOneTimePasswordUtil.qrImageUrl(currentUserObj.getUsername(), base32Secret);

        // we can use the code here and compare it against user input
        UserAccount byUserId = userAccountRepository.findByUserId(currentUser.getCurrentUser().getId());
        byUserId.setTwoStepCode(base32Secret);
        byUserId.setTwoStepActivated(false);
        userAccountRepository.unsafeUpdateUserAccount(byUserId);

        secondFactorResponse.setImage(imageUrl);
        return secondFactorResponse;
    }

    static class SecondFactorResponse {

        private String image;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

}
