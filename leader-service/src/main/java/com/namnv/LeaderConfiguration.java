package com.namnv;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.namnv.core.*;
import com.namnv.core.command.*;
import com.namnv.core.reply.*;
import com.namnv.core.state.StateMachineManager;
import com.namnv.repo.AccountRepository;
import com.namnv.repo.offset.Offset;
import com.namnv.repo.offset.SnapshotRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
public class LeaderConfiguration {
  private final AccountRepository accountRepository;
  private final CommandLogProducerProvider commandLogProducerProvider;
  private final ClusterKafkaConfig clusterKafkaConfig;
  private final TransactionManager transactionManager;
  private final SnapshotRepository snapshotRepository;
  private LeaderBootstrap leaderBootstrap;

  @Bean
  CommandLogKafkaProperties commandLogKafkaProperties() {
    var properties = new CommandLogKafkaProperties();
    properties.setTopic(clusterKafkaConfig.getTopic());
    properties.setGroupId(clusterKafkaConfig.getGroupId());
    return properties;
  }

  @Bean
  public Balances balances() {
    var balances = new Balances();
    balances.setLastedId(accountRepository.lastedId());
    return balances;
  }

  @Bean
  public Offset offset() {
    var offset = new Offset();
    offset.setOffset(snapshotRepository.getLastOffset());
    return offset;
  }

  @Bean
  CommandBufferHandler commandBufferHandler(CommandHandler commandHandler) {
    return new CommandBufferHandlerImpl(commandHandler);
  }

  @Bean
  CommandBufferReply commandBufferReply(ReplyBufferEventDispatcher replyBufferEventDispatcher) {
    return new CommandBufferReplyImpl(replyBufferEventDispatcher);
  }

  @Bean
  KafkaProducer<String, byte[]> producer() {
    return commandLogProducerProvider.initProducer(null);
  }

  @Bean
  CommandBufferJournaler commandBufferJournaler(
      KafkaProducer<String, byte[]> producer,
      CommandLogKafkaProperties commandLogKafkaProperties,
      LeaderProperties leaderProperties) {
    return new CommandBufferJournalerImpl(producer, commandLogKafkaProperties, leaderProperties);
  }

  @Bean
  LeaderProperties leaderProperties(
      @Value("${leader.commandBufferPow}") int commandBufferPow,
      @Value("${leader.replyBufferPow}") int replyBufferPow,
      @Value("${leader.logsChunkSize}") int logsChunkSize) {
    return new LeaderProperties(1 << commandBufferPow, 1 << replyBufferPow, logsChunkSize);
  }

  @Bean
  Disruptor<CommandBufferEvent> commandBufferDisruptor(
      CommandBufferJournaler commandBufferJournaler,
      CommandBufferHandler commandBufferHandler,
      CommandBufferReply commandBufferReply) {
    return new CommandBufferDisruptorDSL(
            commandBufferJournaler, commandBufferHandler, commandBufferReply)
        .build(1 << 15, new YieldingWaitStrategy());
  }

  @Bean
  CommandBufferEventDispatcher commandBufferEventDispatcher(
      Disruptor<CommandBufferEvent> commandBufferEventDisruptor) {
    return new CommandBufferEventDispatcherImpl(commandBufferEventDisruptor);
  }

  @Bean
  CommandHandler commandHandler(Balances balances) {
    return new CommandHandlerImpl(balances);
  }

  @Bean
  ReplyBufferEventDispatcher replyBufferEventDispatcher(
      Disruptor<ReplyBufferEvent> replyBufferEventDisruptor) {
    return new ReplyBufferEventDispatcherImpl(replyBufferEventDisruptor);
  }

  @Bean
  Disruptor<ReplyBufferEvent> replyBufferEventDisruptor(ReplyBufferHandler replyBufferHandler) {
    return new ReplyBufferDisruptorDSL(replyBufferHandler)
        .build(1 << 16, new YieldingWaitStrategy());
  }

  @Bean
  CommandLogConsumerProvider commandLogConsumerProvider() {
    return new CommandLogConsumerProviderAdapter(clusterKafkaConfig);
  }

  @Bean
  StateMachineManager stateMachineManager(
      CommandLogConsumerProvider commandLogConsumerProvider,
      CommandHandler commandHandler,
      Balances balances,
      Offset offset,
      CommandLogKafkaProperties commandLogKafkaProperties) {
    var stateMachine =
        new StateMachineManagerImpl(
            transactionManager,
            accountRepository,
            snapshotRepository,
            commandLogConsumerProvider,
            commandHandler,
            balances,
            offset);
    stateMachine.setCommandLogKafkaProperties(commandLogKafkaProperties);
    return stateMachine;
  }

  @Bean
  LeaderBootstrap leaderBootstrap(
      StateMachineManager stateMachineManager,
      Disruptor<CommandBufferEvent> commandBufferDisruptor,
      Disruptor<ReplyBufferEvent> replyBufferEventDisruptor) {
    leaderBootstrap =
        new LeaderBootstrap(stateMachineManager, commandBufferDisruptor, replyBufferEventDisruptor);
    return leaderBootstrap;
  }

  @EventListener(ApplicationReadyEvent.class)
  void startLeader() {
    leaderBootstrap.onStart();
  }

  @PreDestroy
  void stopLeader() {
    leaderBootstrap.onStop();
  }
}
