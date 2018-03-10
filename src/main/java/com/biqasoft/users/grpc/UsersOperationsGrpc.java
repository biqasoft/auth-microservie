package com.biqasoft.users.grpc;

import com.biqasoft.users.authenticate.RequestAuthenticateService;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.authenticate.dto.AuthenticateResult;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.useraccount.UserAccount;
import com.biqasoft.users.useraccount.UserAccountRepository;
import io.grpc.stub.*;
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
            AuthenticateResult authenticateResult = authenticateService.authenticateRequest(authenticateRequest);
            builder.setAuthenticated(authenticateResult.getAuthenticated());

            if (authenticateResult.getAuths() != null && authenticateResult.getUserAccount() != null) {
                builder.addAllAuths(authenticateResult.getAuths());
                builder.setUserAccount(UsersToGrpcMapper.mapMsModelToGrpc(authenticateResult.getUserAccount()));
            }
        } catch (BiqaAuthenticationLocalizedException e) {
            builder.setError(e.getMessage());
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
        UserAccount internalUser = userAccountRepository.unsafeFindUserById(id).block();

        if (internalUser != null) {
            return UsersToGrpcMapper.mapMsModelToGrpc(internalUser);
        }
        return null;
    }

}
