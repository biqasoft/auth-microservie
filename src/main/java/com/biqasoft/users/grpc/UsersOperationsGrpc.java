package com.biqasoft.users.grpc;

import com.biqasoft.users.auth.TransformUserAccountEntity;
import com.biqasoft.users.authenticate.RequestAuthenticateService;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResponse;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ya on 3/2/2017.
 */
@Service
public class UsersOperationsGrpc extends UsersGrpc.UsersImplBase {

    private final UserAccountRepository userAccountRepository;
    private final RequestAuthenticateService authenticateService;

    @Autowired
    public UsersOperationsGrpc(UserAccountRepository userAccountRepository, RequestAuthenticateService authenticateService) {
        this.userAccountRepository = userAccountRepository;
        this.authenticateService = authenticateService;
    }

    @Override
    public void authenticateUser(UsersGet.UserAuthenticateRequest request, StreamObserver<UsersGet.UserAuthenticateResponse> responseObserver) {
        AuthenticateRequest authenticateRequest = new AuthenticateRequest();
        authenticateRequest.setIp(request.getIp());
        authenticateRequest.setPassword(request.getPassword());
        authenticateRequest.setToken(request.getToken());
        authenticateRequest.setUsername(request.getUsername());

        UsersGet.UserAuthenticateResponse.Builder builder = UsersGet.UserAuthenticateResponse.newBuilder();

        try {
            AuthenticateResponse authenticateResponse = authenticateService.authenticateResponse(authenticateRequest);
            builder.setAuthenticated(authenticateResponse.getAuthenticated());

            if (authenticateResponse.getAuths() != null && authenticateResponse.getUserAccount() != null) {
                builder.addAllAuths(authenticateResponse.getAuths());
                builder.setUserAccount(transform(TransformUserAccountEntity.transform(authenticateResponse.getUserAccount())));
            }
        } catch (BiqaAuthenticationLocalizedException e) {
            builder.setError(e.getMessage());
            builder.setAuthenticated(false);
        }
        responseObserver.onNext(builder.build());
    }

    private com.biqasoft.users.grpc.UserOuterClass.User transform(UserAccount internalUser) {
        UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();

        account.setStatus(internalUser.getStatus());
        account.setId(internalUser.getId());
        account.setEnabled(internalUser.getEnabled());

        if (internalUser.getFirstname() != null) {
            account.setFirstname(internalUser.getFirstname());
        }

        if (internalUser.getLastname() != null) {
            account.setLastname(internalUser.getLastname());
        }

        if (internalUser.getPatronymic() != null) {
            account.setPatronymic(internalUser.getPatronymic());
        }

        if (internalUser.getEmail() != null) {
            account.setEmail(internalUser.getEmail());
        }

        if (internalUser.getTelephone() != null) {
            account.setTelephone(internalUser.getTelephone());
        }

        if (internalUser.getName() != null) {
            account.setName(internalUser.getName());
        }

        if (internalUser.getAvatarUrl() != null) {
            account.setAvatarUrl(internalUser.getAvatarUrl());
        }

//        if (internalUser.getRoles() != null) {
//            account.addAllRoles(internalUser.getRoles());
//        }

        if (internalUser.getLanguage() != null) {
            account.setLanguage(internalUser.getLanguage());
        }

        if (internalUser.getLastOnline() != null) {
            account.setLastOnline(internalUser.getLastOnline().getTime());
        }

        if (internalUser.getUsername() != null) {
            account.setUsername(internalUser.getUsername());
        }

        return account.build();
    }

    @Override
    public void getUserById(UsersGet.UserGetRequest request, StreamObserver<UsersGet.UserGetResponse> responseObserver) {
        UsersGet.UserGetResponse.Builder responseBuilder = UsersGet.UserGetResponse.newBuilder();

        UserOuterClass.User value = processUser(request);
        if (value == null) {
            responseBuilder.setError("user.not.found");
            responseBuilder.setValid(false);
        } else {
            responseBuilder.setValid(true);
            responseBuilder.setUser(value);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private UserOuterClass.User processUser(UsersGet.UserGetRequest request) {
        String id = request.getId();
        UserAccount internalUser = userAccountRepository.unsafeFindUserById(id);

        if (internalUser != null) {
            UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();

            account.setStatus(internalUser.getStatus());
            account.setId(internalUser.getId());
            account.setEnabled(internalUser.getEnabled());

            if (internalUser.getFirstname() != null) {
                account.setFirstname(internalUser.getFirstname());
            }

            if (internalUser.getLastname() != null) {
                account.setLastname(internalUser.getLastname());
            }

            if (internalUser.getPatronymic() != null) {
                account.setPatronymic(internalUser.getPatronymic());
            }

            if (internalUser.getEmail() != null) {
                account.setEmail(internalUser.getEmail());
            }

            if (internalUser.getTelephone() != null) {
                account.setTelephone(internalUser.getTelephone());
            }

            if (internalUser.getName() != null) {
                account.setName(internalUser.getName());
            }

            if (internalUser.getAvatarUrl() != null) {
                account.setAvatarUrl(internalUser.getAvatarUrl());
            }

//            if (internalUser.getRoles() != null) {
//                account.addAllRoles(internalUser.getRoles());
//            }

            if (internalUser.getLanguage() != null) {
                account.setLanguage(internalUser.getLanguage());
            }

            if (internalUser.getLastOnline() != null) {
                account.setLastOnline(internalUser.getLastOnline().getTime());
            }

            if (internalUser.getUsername() != null) {
                account.setUsername(internalUser.getUsername());
            }

            return account.build();
        }
        return null;
    }

}
