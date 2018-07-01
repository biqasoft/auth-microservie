package com.biqasoft.users.useraccount;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.users.auth.UserAccountMapper;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.domain.Domain;
import com.biqasoft.users.domain.DomainRepository;
import com.biqasoft.users.notifications.EmailPrepareAndSendService;
import com.biqasoft.users.useraccount.dbo.UserAccount;
import com.biqasoft.users.useraccount.dto.UserAccountRegisterRequestDto;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Api("Registration domain and first user")
@RestController
@RequestMapping(RegistrationController.RegistrationController_BASE)
public class RegistrationController {

    public static final String RegistrationController_BASE = "/v1/registration";
    public static final String RegistrationController_REGISTER = "register";

    private final DomainRepository domainRepository;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final UserAccountRepository userAccountRepository;

    public RegistrationController(DomainRepository domainRepository, EmailPrepareAndSendService emailPrepareAndSendService, UserAccountRepository userAccountRepository) {
        this.domainRepository = domainRepository;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.userAccountRepository = userAccountRepository;
    }

    @ApiOperation(value = "register new user in new domain with admin role")
    @PostMapping(value = RegistrationController_REGISTER)
    public Mono<GlobalUserController.CreatedUserDto> register(@Valid @RequestBody UserAccountRegisterRequestDto userAccountAddRequest) {
        return userAccountRepository.findByUsernameOrOAuthToken(userAccountAddRequest.getUserAccount().getUsername()).map(__ -> {
            // user with same email already exist
            throw  ThrowAuthExceptionHelper.throwJustExceptionBiqaAuthenticationLocalizedException("useraccount.create.username.already.exists");
        })

                // TODO: hack: map just to fix mono type; if there is a value - above there is an exception, so map will nevert be actually executed
                .map(dg -> new GlobalUserController.CreatedUserDto())
                .switchIfEmpty(Mono.defer(() -> {

                    Domain domain = domainRepository.addDomain(null);

                    // create new admin account
                    UserAccount user = new UserAccount();

                    user.setTelephone(userAccountAddRequest.getUserAccount().getTelephone());
                    user.setUsername(userAccountAddRequest.getUserAccount().getEmail());
                    user.setFirstname(userAccountAddRequest.getUserAccount().getFirstname());
                    user.setLastname(userAccountAddRequest.getUserAccount().getLastname());
                    user.setEmail(userAccountAddRequest.getUserAccount().getEmail());

                    user.setRoles(Lists.newArrayList(SystemRoles.ROLE_ADMIN, SystemRoles.ALLOW_ALL_DOMAIN_BASED));
                    user.setDomain(domain.getDomain());

                    if (StringUtils.isEmpty(userAccountAddRequest.getPassword())) {
                        user.setPassword(userAccountAddRequest.getPassword());
                    }

                    return userAccountRepository.registerNewUser(user).flatMap(createdUserInternal -> {
                        GlobalUserController.CreatedUserDto response = new GlobalUserController.CreatedUserDto();
                        response.setDomain(createdUserInternal.getDomain());
                        response.setPassword(createdUserInternal.getPassword());
                        response.setUserAccount(UserAccountMapper.mapInternalToDto(createdUserInternal.getUserAccount()));

                        if (userAccountAddRequest.isSendWelcomeEmail()) {
                            emailPrepareAndSendService.sendWelcomeEmailWhenCreateNewDomain(createdUserInternal.getUserAccount(), createdUserInternal.getPassword());
                        }
                        return Mono.just(response);
                    });
                }));

    }

}
