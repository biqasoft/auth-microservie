package com.biqasoft.users.distributedstorage;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * We init services through consul to separate microservices
 * but after - hazelcast auto add/remove from this cluster nodes
 * So, on network split, you do not need to update JoinConfig of hazelcast from nodes in service discovery(Consul)
 *
 * Created by Nikita on 14.08.2016.
 */
@Service
@ConditionalOnProperty("biqa.auth.limits.interval.fail.enable")
public class HazelcastService {

    private final DiscoveryClient discoveryClient;
    private HazelcastInstance client;

    @Autowired
    public HazelcastService(@Value("${spring.cloud.consul.discovery.service-name}") String serviceName, DiscoveryClient discoveryClient) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        Config config = new Config();
        NetworkConfig network = config.getNetworkConfig();
        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);

        join.getTcpIpConfig().setEnabled(true);
        instances.forEach(x -> {
            join.getTcpIpConfig().addMember(x.getHost());
        });

        this.client = Hazelcast.newHazelcastInstance(config);
        this.discoveryClient = discoveryClient;
    }

    public HazelcastInstance getClient() {
        return client;
    }

}
