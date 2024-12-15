package com.namnv.grpc;

import com.google.common.util.concurrent.MoreExecutors;
import com.namnv.core.Balances;
import com.namnv.core.SimpleReplier;
import com.namnv.core.command.CommandBufferEventDispatcher;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BalanceGrpcResourceLeader {
  private final Balances balances;
  private final SimpleReplier replier;
  private final CommandBufferEventDispatcher commandBufferEventDispatcher;
  private Server server;

  @Value("${server.grpc.port}")
  private int serverPort;

  public BalanceGrpcResourceLeader(
      Balances balances,
      SimpleReplier replier,
      CommandBufferEventDispatcher commandBufferEventDispatcher) {
    this.balances = balances;
    this.replier = replier;
    this.commandBufferEventDispatcher = commandBufferEventDispatcher;
    try {
      var balanceCommandGrpc = new BalanceGrpcCommand(commandBufferEventDispatcher, replier);
      var balanceQueryGrpc = new BalanceGrpcQuery(balances);
      server =
          ServerBuilder.forPort(9091)
              .addService(balanceQueryGrpc)
              .addService(balanceCommandGrpc)
              .executor(MoreExecutors.directExecutor())
              .build();
      server.start();
    } catch (Exception e) {
      System.exit(-9);
    }
  }

  @PostConstruct
  void init() {
    //        try {
    //            BalanceGrpcCommand balanceCommandGrpc = new BalanceGrpcCommand(replier);
    //            server = ServerBuilder.forPort(serverPort)
    //                    .addService(balanceCommandGrpc)
    //                    .executor(MoreExecutors.directExecutor())
    //                    .build();
    //            server.start();
    //        } catch (Exception e) {
    //            log.error("Failed to start gRPC server", e);
    //            System.exit(-9);
    //        }
  }

  @PreDestroy
  void destroy() {
    if (server != null) {
      server.shutdown();
    }
  }
}
