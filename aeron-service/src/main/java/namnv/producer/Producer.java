package namnv.producer;


import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static namnv.ClusterNodeConfig.CLIENT_FACING_PORT_OFFSET;
import static namnv.ClusterNodeConfig.calculatePort;

public class Producer implements EgressListener {
    private final MutableDirectBuffer actionBidBuffer = new ExpandableArrayBuffer();
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();

    private long nextCorrelationId = 0;

    public Producer() {
    }

    public void start(AeronCluster aeronCluster) {
        Thread thread = new Thread(() -> {
            while (true) {
                idleStrategy.idle(aeronCluster.pollEgress());
            }
        });
        thread.setDaemon(true);
        thread.setName("client-poller");
        thread.start();
    }

    public void onMessage(
            long clusterSessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int length,
            Header header) {
        printOutput("Response: " + Instant.now() + ", " + buffer.getStringWithoutLengthAscii(offset, length));
    }

    public void onSessionEvent(
            long correlationId,
            long clusterSessionId,
            long leadershipTermId,
            int leaderMemberId,
            EventCode code,
            String detail) {
        printOutput(
                "SessionEvent(" + correlationId + ", " + leadershipTermId + ", " +
                        leaderMemberId + ", " + code + ", " + detail + ")");
    }

    public void onNewLeader(
            long clusterSessionId,
            long leadershipTermId,
            int leaderMemberId,
            String ingressEndpoints) {
        printOutput("NewLeader(" + clusterSessionId + ", " + leadershipTermId + ", " + leaderMemberId + ")");
    }


    private long sendMessage(AeronCluster aeronCluster) {
        long correlationId = nextCorrelationId++;

        int offset = 0;
        actionBidBuffer.putLong(offset, correlationId);
        offset += BitUtil.SIZE_OF_LONG;

        actionBidBuffer.putLong(offset, 1);
        offset += BitUtil.SIZE_OF_LONG;

        byte[] fullName = "nguyen van nam".getBytes(StandardCharsets.UTF_8);
        actionBidBuffer.putInt(offset, fullName.length);
        offset += BitUtil.SIZE_OF_INT;

        actionBidBuffer.putBytes(offset, fullName);
        offset += fullName.length;

        idleStrategy.reset();
        while (aeronCluster.offer(actionBidBuffer, 0, offset) < 0) {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
        printOutput("Sent: " + Instant.now() + ", correlationId: " + correlationId);

        return correlationId;
    }

    public static String ingressEndpoints(List<String> hostnames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++) {
            sb.append(i).append('=');
            sb.append(hostnames.get(i)).append(':').append(calculatePort(i, CLIENT_FACING_PORT_OFFSET));
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private void printOutput(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        String[] hostnames = new String[]{"172.16.0.5"};
        String ingressEndpoints = ingressEndpoints(Arrays.asList(hostnames));

        Producer client = new Producer();

        try (MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true)
                .dirDeleteOnShutdown(true));

             AeronCluster aeronCluster = AeronCluster.connect(
                     new AeronCluster.Context()
                             .egressListener(client)
                             .egressChannel("aeron:udp?endpoint=239.255.255.1:4300|interface=172.16.0.8|ttl=16")
                             .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                             .ingressChannel("aeron:udp")
                             .ingressEndpoints(ingressEndpoints))) {
            client.start(aeronCluster);
            for (int i = 0; i < 2; i++) {
                client.sendMessage(aeronCluster);
            }

            while (true) {
            }
        }
    }
}
