package com.biqasoft.users.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by ya on 3/2/2017.
 */
@Service
public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    /**
     * Create a RouteGuide server using serverBuilder as a base and features as data.
     */
    @Autowired
    public GrpcServer(UsersOperationsGrpc usersOperationsGrpc, @Value("${server.grpc.port}") int port) {
        ServerBuilder<?> o = ServerBuilder.forPort(port).addService(usersOperationsGrpc);
        Server build = o.build();

        try {
            logger.info("grpc server started, listening on " + port);
            build.start();
        } catch (IOException e) {
            logger.error("Error in grpc", e);
        }
    }

}
