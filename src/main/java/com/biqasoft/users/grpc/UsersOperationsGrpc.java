package com.biqasoft.users.grpc;

import com.biqasoft.auth.internal.grpc.UserOuterClass;
import com.biqasoft.auth.internal.grpc.UsersGet;
import com.biqasoft.auth.internal.grpc.UsersGrpc;
import com.biqasoft.users.authenticate.RequestAuthenticateService;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.config.BiqaAuthenticationLocalizedException;
import com.biqasoft.users.useraccount.UserAccountRepository;
import com.biqasoft.users.useraccount.dbo.UserAccountDbo;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ya on 3/2/2017.
 */
@Service
public class UsersOperationsGrpc extends UsersGrpc.UsersImplBase {

    private final UserAccountRepository userAccountRepository;
    private final RequestAuthenticateService authenticateService;
    private static final Logger logger = LoggerFactory.getLogger(UsersOperationsGrpc.class);

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
            authenticateService.authenticateRequest(authenticateRequest)
                    .doOnError((e) -> {
                        if (e instanceof BiqaAuthenticationLocalizedException) {
                            builder.setError(e.getMessage());
                            builder.setAuthenticated(false);
                            responseObserver.onNext(builder.build());
                        } else {
                            logger.error("internal error auth grpc", e);
                            builder.setError(e.getMessage());
                            builder.setAuthenticated(false);
                            responseObserver.onNext(builder.build());
                        }
                    })
                    .subscribe(authenticateResult -> {
                        builder.setAuthenticated(authenticateResult.getAuthenticated());

                        if (authenticateResult.getAuths() != null && authenticateResult.getUserAccount() != null) {
                            builder.addAllAuths(authenticateResult.getAuths());
                            builder.setUserAccount(UsersToGrpcMapper.mapMsModelToGrpc(authenticateResult.getUserAccount()));
                        }
                        responseObserver.onNext(builder.build());
                    });
        } catch (Exception e) {
            builder.setError(e.getMessage());
            builder.setAuthenticated(false);
            responseObserver.onNext(builder.build());
        }
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
        UserAccountDbo internalUser = userAccountRepository.unsafeFindUserById(id).block();

        if (internalUser != null) {
            return UsersToGrpcMapper.mapMsModelToGrpc(internalUser);
        }
        return null;
    }

}
