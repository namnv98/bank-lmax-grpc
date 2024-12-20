package com.namnv.infra.cluster;

import com.namnv.proto.BalanceCommandServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thaivc
 * @since 2024
 */
@Configuration
public class LeaderClusterCommandConfig {

    @Value("${cluster.leader.host}")
    private String leaderHost;

    @Value("${cluster.leader.port}")
    private int leaderPort;

    @Bean
    public BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub() {
        return BalanceCommandServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress(leaderHost, leaderPort)
                .usePlaintext()
                .build()
        );
    }
}
