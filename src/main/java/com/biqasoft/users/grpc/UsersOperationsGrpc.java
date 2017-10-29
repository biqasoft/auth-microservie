package com.biqasoft.users.grpc;

import com.biqasoft.users.auth.UserAccountMapper;
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
                builder.setUserAccount(UserToGrpcMapper.transform(UserAccountMapper.transform(authenticateResponse.getUserAccount())));
            }
        } catch (BiqaAuthenticationLocalizedException e) {
            builder.setError(e.getErrorResource().getCode());
            builder.setAuthenticated(false);
        }
        responseObserver.onNext(builder.build());
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
            UserOuterClass.User.Builder account = UserToGrpcMapper.mapUserToGrpc(internalUser);
            return account.build();
        }
        return null;
    }

}
