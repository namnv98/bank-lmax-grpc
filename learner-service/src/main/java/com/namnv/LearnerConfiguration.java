package com.namnv;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.namnv.core.Balances;
import com.namnv.core.ClusterKafkaConfig;
import com.namnv.core.CommandLogConsumerProvider;
import com.namnv.core.CommandLogKafkaProperties;
import com.namnv.repo.AccountRepository;
import com.namnv.core.command.CommandHandler;
import com.namnv.core.command.CommandHandlerImpl;
import com.namnv.repo.offset.Offset;
import com.namnv.repo.offset.SnapshotRepository;
import com.namnv.core.replay.*;
import com.namnv.core.state.StateMachineManager;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LearnerConfiguration {

    private final SnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final ClusterKafkaConfig clusterKafkaConfig;
    private final TransactionManager transactionManager;

    private LearnerBootstrap learnerBootstrap;

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
        balances.setEnableChangedCapture(true);
        return balances;
    }

    @Bean
    public Offset offset() {
        var offset = new Offset();
        offset.setOffset(snapshotRepository.getLastOffset());
        return offset;
    }

    @Bean
    CommandHandler commandHandler(Balances balances) {
        return new CommandHandlerImpl(balances);
    }

    @Bean
    StateMachineManager stateMachineManager(CommandLogConsumerProvider commandLogConsumerProvider, CommandHandler commandHandler, Balances balances, Offset offset, CommandLogKafkaProperties commandLogKafkaProperties) {
        var stateMachine = new StateMachineManagerImpl(transactionManager, accountRepository, snapshotRepository, commandLogConsumerProvider, commandHandler, balances, offset);
        stateMachine.setCommandLogKafkaProperties(commandLogKafkaProperties);
        return stateMachine;
    }

    @Bean
    LearnerProperties learnerProperties(
            @Value("${learner.bufferSize}") int bufferSize,
            @Value("${learner.pollInterval}") int pollInterval,
            @Value("${learner.maxSnapshotCheckCircles}") int maxSnapshotCheckCircles,
            @Value("${learner.snapshotFragmentSize}") int snapshotFragmentSize,
            @Value("${learner.snapshotLifeTime}") int snapshotLifeTime
    ) {
        var learnerProperties = new LearnerProperties();
        learnerProperties.setBufferSize(bufferSize);
        learnerProperties.setPollingInterval(pollInterval);
        learnerProperties.setMaxSnapshotCheckCircles(maxSnapshotCheckCircles);
        learnerProperties.setSnapshotFragmentSize(snapshotFragmentSize);
        learnerProperties.setSnapshotLifeTime(Duration.ofSeconds(snapshotLifeTime));
        return learnerProperties;
    }

    @Bean
    ReplayBufferHandler replayBufferHandlerByLearner(CommandHandler commandHandler, StateMachineManager stateMachineManager, LearnerProperties learnerProperties) {
        var replayHandler = new ReplayBufferHandlerByLearner(commandHandler, stateMachineManager, learnerProperties);
        replayHandler.setEventCount(learnerProperties.getBufferSize());
        return replayHandler;
    }

    @Bean
    Disruptor<ReplayBufferEvent> replayBufferEventDisruptor(ReplayBufferHandler replayBufferHandler) {
        return new ReplayBufferDisruptorDSL(replayBufferHandler).build(2048, new SleepingWaitStrategy());
    }

    @Bean
    LearnerBootstrap learnerBootstrap(
            StateMachineManager stateMachineManager,
            Disruptor<ReplayBufferEvent> replayBufferEventDisruptor,
            CommandLogConsumerProvider commandLogConsumerProvider,
            Offset offset,
            CommandLogKafkaProperties commandLogKafkaProperties,
            ReplayBufferEventDispatcher replayBufferEventDispatcher,
            LearnerProperties learnerProperties
    ) {
        learnerBootstrap = new LearnerBootstrap(
                stateMachineManager,
                replayBufferEventDisruptor,
                commandLogConsumerProvider,
                offset,
                replayBufferEventDispatcher,
                learnerProperties
        );
        learnerBootstrap.setCommandLogKafkaProperties(commandLogKafkaProperties);
        return learnerBootstrap;
    }

    @Bean
    ReplayBufferEventDispatcher replayBufferEventDispatcher(Disruptor<ReplayBufferEvent> replayBufferEventDisruptor) {
        return new ReplayBufferEventDispatcherImpl(replayBufferEventDisruptor);
    }

    @Bean
    CommandLogConsumerProvider commandLogConsumerProvider() {
        return new CommandLogConsumerProviderAdapter(clusterKafkaConfig);
    }

    @EventListener(ApplicationReadyEvent.class)
    void startLearner() {
        log.info("Bootstrapping learner");
        learnerBootstrap.onStart();
    }

    @PreDestroy
    void stopLearner() {
        log.info("Destroying learner");
        learnerBootstrap.onStop();
    }

}
