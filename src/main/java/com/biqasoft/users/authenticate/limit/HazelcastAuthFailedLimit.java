package com.biqasoft.users.authenticate.limit;

import com.biqasoft.common.watchablevalue.WatchableValue;
import com.biqasoft.users.authenticate.dto.AuthenticateRequest;
import com.biqasoft.users.config.ThrowAuthExceptionHelper;
import com.biqasoft.users.distributedstorage.HazelcastService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ISet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nikita on 14.08.2016.
 */
@Service
@Primary
@ConditionalOnProperty("biqa.auth.limits.interval.fail.enable")
public class HazelcastAuthFailedLimit implements AuthFailedLimit {

    private final HazelcastService hazelcastService;
    private final static String FAILED_AUTH_PREFIX = "auth-failed-";
    private final static String BANNED_AUTH_PREFIX = "auth-banned-";
    private final int MAX_FAILED_AUTH;
    private final HazelcastInstance client;

    @WatchableValue
    private Boolean enableFailedLimit;

    private static final Logger logger = LoggerFactory.getLogger(HazelcastAuthFailedLimit.class);
    private final ISet<String> bannedSet;
    private final ISet<String> set;
    private ILock lock;

    @Autowired
    public HazelcastAuthFailedLimit(HazelcastService hazelcastService,
                                    @Value("${biqa.auth.limits.interval.fail.times}") Integer maxFailedPerMinuteLimit,
                                    @Value("${biqa.auth.limits.interval.fail.enable}") Boolean failedLimitEnable) {
        this.hazelcastService = hazelcastService;
        this.client = hazelcastService.getClient();
        this.MAX_FAILED_AUTH = maxFailedPerMinuteLimit;
        this.enableFailedLimit = !failedLimitEnable;

        this.bannedSet = hazelcastService.getClient().getSet(BANNED_AUTH_PREFIX);
        this.set = hazelcastService.getClient().getSet(FAILED_AUTH_PREFIX);
        this.lock = hazelcastService.getClient().getLock("auth-failed-lock-clean");
    }

    @PreDestroy
    public void preDestroy() {
        lock.unlock();
    }

    @Scheduled(cron = "${biqa.auth.limits.interval}")
    public void scheduledCleanFailedAuthLimits() {

        try {
            if (lock.tryLock(2000, TimeUnit.MILLISECONDS, 64000, TimeUnit.MILLISECONDS) || lock.isLockedByCurrentThread()){
                    {
                        logger.debug("Leader for clean banned auth");
                        logger.info("Failed auth clients {}", set.size());

                        set.forEach(x -> {
                            hazelcastService.getClient().getAtomicLong(getLongKeyByRemoteAddress(x)).destroy();
                        });
                        set.clear();
                        logger.debug("END: clean failed auth");
                    }

                    {
                        logger.debug("Start: clean banned auth");
                        logger.debug("banned auth size {}", bannedSet.size());
                        bannedSet.clear();
                        logger.debug("END: clean Banned auth");
                    }

            }else{
                logger.debug("Not I have lock for clean auth");
            }

        } catch (InterruptedException e) {
            logger.error("Error get lock", e);
        }

    }

    @Override
    public void processFailedAuth(AuthenticateRequest authenticateRequest) {
        if (!enableFailedLimit) return;

        String remoteAddress = authenticateRequest.getIp();
        if (!StringUtils.isEmpty(remoteAddress)) {
            IAtomicLong atomicLong = hazelcastService.getClient().getAtomicLong(getLongKeyByRemoteAddress(remoteAddress));
            long failedTimes = atomicLong.incrementAndGet();

            if (failedTimes == 1) {
                set.add(remoteAddress);
            }

            if (failedTimes > MAX_FAILED_AUTH) {
                bannedSet.add(remoteAddress);
                logger.info("ban remote auth failed {} time {}", remoteAddress, failedTimes);
            }

        }
    }

    private static String getLongKeyByRemoteAddress(String remoteAddress) {
        return FAILED_AUTH_PREFIX + "long-" + remoteAddress;
    }

    @Override
    public void checkAuthFailedLimit(AuthenticateRequest authenticateRequest) {
        if (!enableFailedLimit) return;

        String remoteAddress = authenticateRequest.getIp();
        if (!StringUtils.isEmpty(remoteAddress)) {
            if (!bannedSet.contains(remoteAddress)) {
                return;
            } else {
                ThrowAuthExceptionHelper.throwExceptionBiqaAuthenticationLocalizedException("auth.exception.failed.limit");
            }
        }
    }
}