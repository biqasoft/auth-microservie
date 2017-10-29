package com.biqasoft.users.authenticate.chain;

import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.oauth2.OAuth2Repository;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UsernamePasswordAuthFilter implements AuthChainFilter {

    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthFilter.class);

    private final OAuth2Repository oAuth2Repository;
    private final AuthFailedLimit authFailedLimit;
    private final UserAccountRepository userAccountRepository;
    private final UserAuthChecks userAuthChecks;
    private final PasswordEncoder encoder;

    @Autowired
    public UsernamePasswordAuthFilter(OAuth2Repository oAuth2Repository, AuthFailedLimit authFailedLimit, UserAccountRepository userAccountRepository, UserAuthChecks userAuthChecks, PasswordEncoder encoder) {
        this.oAuth2Repository = oAuth2Repository;
        this.authFailedLimit = authFailedLimit;
        this.userAccountRepository = userAccountRepository;
        this.userAuthChecks = userAuthChecks;
        this.encoder = encoder;
    }

    @Override
    public AuthChainOneFilterResult process(AuthenticateRequest authenticateRequest) {

        AuthChainOneFilterResult result = new AuthChainOneFilterResult();
        UserAccount user;

        String password = authenticateRequest.getPassword();
        String username = authenticateRequest.getUsername();

        List<GrantedAuthority> auths;

        user = userAccountRepository.findByUsernameOrOAuthToken(username);
        if (user == null) {
            logger.info("Username {}: user not found", username);
            authFailedLimit.processFailedAuth(authenticateRequest);
            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.no_user");
        }

        // plain text password with active 2 step auth require send as token
        if (user.isTwoStepActivated()) {
            String token = authenticateRequest.getToken();
            if (StringUtils.isEmpty(token)) {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
            }

            UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(token);
            if (userNameWithPassword != null && !StringUtils.isEmpty(userNameWithPassword.getTwoStepCode())) {

                if (!userAuthChecks.isTwoStepCodeValidForUser(user, userNameWithPassword.twoStepCode)) {
                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                }

            } else {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
            }
        }

//         TODO: root user
//         encoder.matches() is hash - long operation
        if (!encoder.matches(password, user.getPassword())) {
            isRootUser = userAuthChecks.checkRootAccount(username, password);
        }

        result.getAuthenticateResponse().setUserAccount(user);
        result.getAuthenticateResponse().setAuths(user.getRoles());
        // TODO: set domain
        result.setSuccessProcessed(true);

        return result;
    }

}
