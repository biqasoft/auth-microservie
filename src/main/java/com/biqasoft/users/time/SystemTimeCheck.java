package com.biqasoft.users.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Check time from google and compare with local system time
 *
 * 2FA requires sync time between client which generates code with server with also check and verify that code
 **/
@Service
@ConditionalOnProperty("biqa.time.check")
public class SystemTimeCheck {

    private static final int TIME_DIFF_THRESHOLD = 2000;
    private final GoogleTimeCheck googleTimeCheck;
    private static final Logger logger = LoggerFactory.getLogger(SystemTimeCheck.class);

    public SystemTimeCheck(GoogleTimeCheck googleTimeCheck) {
        this.googleTimeCheck = googleTimeCheck;
    }

    // check every minute
    @Scheduled(cron = "0 * * * * *")
    public void check() {
        try {
            ResponseEntity<byte[]> time = googleTimeCheck.getTime();

            // ignore network latency
            long date = time.getHeaders().getFirstDate("Date");
            Date googleTime = new Date(date);
            Date systemTime = new Date();

            if (Math.abs(systemTime.getTime() - googleTime.getTime()) > TIME_DIFF_THRESHOLD) {
                logger.error("Large time difference. Two factor auth may work incorrect. Google time = {} Current system time is = {}", googleTime.toString(), systemTime.toString());
            }
        } catch (Exception e) {
            logger.warn("Can not get current google time", e);
        }


    }

}
