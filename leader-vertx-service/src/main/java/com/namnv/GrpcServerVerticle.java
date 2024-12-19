package com.namnv;


import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.namnv.core.BaseCommand;
import com.namnv.core.CommandLogKafkaProperties;
import com.namnv.core.LeaderProperties;
import com.namnv.core.command.*;
import com.namnv.core.reply.ReplyBufferEvent;
import com.namnv.core.reply.ReplyBufferEventDispatcher;
import com.namnv.proto.BalanceProto;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetServerOptions;
//import io.vertx.grpc.server.GrpcServer;
//import io.vertx.grpc.server.GrpcServerOptions;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerOptions;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.concurrent.ThreadFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


public class GrpcServerVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Runner.runExample(GrpcServerVerticle.class);
  }

  @Override
  public void start() {
    var a = new GrpcServerOptions();
    a.setMaxMessageSize(1000000);

    GrpcServer grpcServer = GrpcServer.server(vertx, a);

    HttpServer server = vertx.createHttpServer();
    var commandBufferJournaler = new CommandBufferJournalerImpl(initProducer(),
      CommandLogKafkaProperties.builder().groupId("kafka_topic").topic("command-log-leader-group").build(), new LeaderProperties());

    Disruptor<CommandBufferEvent> eventDisruptor =
      new Disruptor<CommandBufferEvent>(
        CommandBufferEvent::new,
        1 << 16,
        DaemonThreadFactory.INSTANCE,
        ProducerType.MULTI,
        new BusySpinWaitStrategy());

    eventDisruptor
      .handleEventsWith(commandBufferJournaler).then(new CommandBufferHandler1Impl());
    eventDisruptor.start();
    var bb = new BaseCommand(BalanceProto.CommandLog.newBuilder().setDepositCommand(BalanceProto.DepositCommand.newBuilder().setAmount(123).setId(123).build()).build());
    com.namnv.HelloReply aa = com.namnv.HelloReply.newBuilder().setMessage("ok").build();


    grpcServer.callHandler(com.namnv.VertxGreeterGrpcServer.SayHello, request -> {
      request.handler(event -> {
        eventDisruptor.publishEvent((commandBufferEvent, l) -> {
          commandBufferEvent.copy(new CommandBufferEvent(
            null, request.response(), bb));
        });
      });
    });

    server.requestHandler(grpcServer).listen(8080).onFailure(cause -> {
      cause.printStackTrace();
    });
  }

  private ThreadFactory createThreadFactory(String namePrefix) {
    var seed = new AtomicInteger(0);
    return r -> new Thread(r, String.format(namePrefix + "-#%d", seed.incrementAndGet()));
  }

  public KafkaProducer<String, byte[]> initProducer() {
    var props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
    return new KafkaProducer<>(props);
  }

  public class CommandBufferHandler1Impl implements CommandBufferHandler {
    com.namnv.HelloReply a = com.namnv.HelloReply.newBuilder().setMessage("ok").build();

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) {
      event.getGrpcServerResponse().end(a);
    }
  }

}

