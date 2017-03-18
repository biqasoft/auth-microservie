package com.biqasoft.users.time;

import com.biqasoft.microservice.communicator.interfaceimpl.annotation.MicroMapping;
import com.biqasoft.microservice.communicator.interfaceimpl.annotation.Microservice;
import org.springframework.http.ResponseEntity;

/**
 * Created by ya on 3/18/2017.
 */
@Microservice("https://google.com")
public interface GoogleTimeCheck {

    @MicroMapping("")
    ResponseEntity<byte[]> getTime();

}
