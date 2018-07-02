package com.biqasoft.users.authenticate.chain.impl;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.users.authenticate.AuthHelper;
import com.biqasoft.users.authenticate.chain.AuthChainFilter;
import com.biqasoft.users.authenticate.chain.AuthChainOneFilterResult;
import com.biqasoft.users.authenticate.chain.UserAuthChecks;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.UserNameWithPassword;
import com.biqasoft.users.authenticate.limit.AuthFailedLimit;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.useraccount.User2FAService;
import com.biqasoft.users.useraccount.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication via username and password
 */
@Service
public class UsernamePasswordAuthFilter implements AuthChainFilter {

    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthFilter.class);

    private final AuthFailedLimit authFailedLimit;
    private final UserAccountRepository userAccountRepository;
    private final UserAuthChecks userAuthChecks;
    private final PasswordEncoder encoder;
    private final User2FAService user2FAService;

    private final DomainRepository domainRepository;

    public UsernamePasswordAuthFilter(AuthFailedLimit authFailedLimit, UserAccountRepository userAccountRepository,
                                      UserAuthChecks userAuthChecks, PasswordEncoder encoder, User2FAService user2FAService,
                                      DomainRepository domainRepository) {
        this.authFailedLimit = authFailedLimit;
        this.userAccountRepository = userAccountRepository;
        this.userAuthChecks = userAuthChecks;
        this.encoder = encoder;
        this.user2FAService = user2FAService;
        this.domainRepository = domainRepository;
    }

    @Override
    public Mono<AuthChainOneFilterResult> process(AuthenticateRequest authenticateRequest) {

        String password = authenticateRequest.getPassword();
        String username = authenticateRequest.getUsername();

        return Mono.create(s -> {
            userAccountRepository.findByUsernameOrOAuthToken(username).doOnSuccessOrError((user, y) -> {
                if (y != null) {
                    logger.error("Internal error", y);
                    s.success(EMPTY_RESULT);
                    // no data
                } else if (user == null) {
                    logger.info("Username {}: user not found", username);
//                    ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.no_user");
                    s.success(EMPTY_RESULT);
                } else {
                    AuthChainOneFilterResult result = new AuthChainOneFilterResult();

                    // plain text password with active 2 step auth require send as token
                    if (user.isTwoStepActivated()) {
                        String token = authenticateRequest.getToken();
                        if (StringUtils.isEmpty(token)) {
                            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                        }

                        UserNameWithPassword userNameWithPassword = AuthHelper.processTokenHeaderToUserNameAndPassword(token);
                        if (userNameWithPassword != null && !StringUtils.isEmpty(userNameWithPassword.getTwoStepCode())) {

                            if (!user2FAService.isTwoStepCodeValidForUser(user, userNameWithPassword.twoStepCode)) {
                                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                            }

                        } else {
                            ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.two_step_require");
                        }
                    }

                    List<String> auths = new ArrayList<>();
                    boolean isRootUser = false;
                    String rootAuthority = SystemRoles.ROOT_USER;

                    //   encoder.matches() is hash - long operation
                    if (!encoder.matches(password, user.getPassword())) {
                        isRootUser = userAuthChecks.checkRootAccount(username, password);

                        if (isRootUser) {
                            auths.add(rootAuthority);
                        } else {
                            s.error(ThrowAuthExceptionHelper.justErrorReactiveBiqaAuthenticationLocalizedException("auth.exception.invalid_password"));
                            return;
                        }
                    }

                    // security check than user can not add ROLE_ROOT user manually
                    if (!isRootUser) {
                        auths.remove(rootAuthority);
                    }

                    result.getAuthenticateResult().setDomain(domainRepository.findDomainById(user.getDomain()));

                    if (!StringUtils.isEmpty(authenticateRequest.getIp())) {
                        userAuthChecks.checkUserIpPatternAndActiveDomain(authenticateRequest.getIp(), user, result.getAuthenticateResult().getDomain());
                    }

                    userAuthChecks.checkUserEnabled(user, authenticateRequest);

                    // do not add domain settings because they not always need
//            result.getAuthenticateResult().setDomainSettings(domainSettingsRepository.findDomainSettingsById(user.getDomain()));

                    result.getAuthenticateResult().setUserAccount(user);
                    result.getAuthenticateResult().setAuths(auths);
                    // TODO: set domain
                    result.setSuccessProcessed(true);
                    result.getAuthenticateResult().setAuthenticated(true);

                    s.success(result);
                }
            }).subscribe();
        });

    }

    @Override
    public String getName() {
        return "UsernamePassword";
    }

    @Override
    public String getDescription() {
        return "Authentication via plain-text username and password";
    }

    @Override
    public boolean is2FASupported() {
        return true;
    }

}
