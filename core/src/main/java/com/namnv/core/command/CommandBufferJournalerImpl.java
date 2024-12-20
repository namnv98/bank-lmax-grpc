package com.namnv.core.command;

import com.namnv.core.CommandLogKafkaProperties;
import com.namnv.core.LeaderProperties;
import com.namnv.proto.BalanceProto;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class CommandBufferJournalerImpl implements CommandBufferJournaler {

    private final KafkaProducer<String, byte[]> producer;
    private final CommandLogKafkaProperties commandLogKafkaProperties;
    private final LeaderProperties leaderProperties;

    private final Deque<List<BalanceProto.CommandLog>> buffers = new ArrayDeque<>();

    public CommandBufferJournalerImpl(
            KafkaProducer<String, byte[]> producer,
            CommandLogKafkaProperties commandLogKafkaProperties,
            LeaderProperties leaderProperties
    ) {
        this.producer = producer;
        this.commandLogKafkaProperties = commandLogKafkaProperties;
        this.leaderProperties = leaderProperties;

        buffers.add(new ArrayList<>(leaderProperties.getLogsChunkSize()));
    }

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (event == null || event.getCommand() == null) return;
        pushToBuffers(event);
        if (endOfBatch) {
            journalCommandLogs();
        }
    }

    private void pushToBuffers(CommandBufferEvent event) {
        if (buffers.getLast().size() == leaderProperties.getLogsChunkSize()) {
            buffers.addLast(new ArrayList<>(leaderProperties.getLogsChunkSize()));
        }
        buffers.getLast().add(event.getCommand().getCommandLog());
    }

    @SneakyThrows
    private void journalCommandLogs() {
        for (List<BalanceProto.CommandLog> commandLogs : buffers) {
            var commandLogsMessage = BalanceProto.CommandLogs.newBuilder()
                    .addAllLogs(commandLogs)
                    .build();
            producer.send(new ProducerRecord<>(commandLogKafkaProperties.getTopic(), commandLogsMessage.toByteArray())).get();
            producer.flush();
        }
        buffers.clear();
        buffers.add(new ArrayList<>(leaderProperties.getLogsChunkSize()));
    }
}
