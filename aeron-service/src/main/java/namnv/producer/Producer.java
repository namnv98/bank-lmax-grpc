package namnv.producer;


import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static namnv.ClusterNodeConfig.*;

public class Producer implements EgressListener {
  private final MutableDirectBuffer actionBidBuffer = new ExpandableArrayBuffer();
  private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
  private final long customerId;

  private long correlationId = ThreadLocalRandom.current().nextLong();
  private long lastBidSeen = 100;

  public Producer(final long customerId) {
    this.customerId = customerId;
  }

  public void onMessage(
    final long clusterSessionId,
    final long timestamp,
    final DirectBuffer buffer,
    final int offset,
    final int length,
    final Header header) {
    final long correlationId = buffer.getLong(offset + CORRELATION_ID_OFFSET);
    final long customerId = buffer.getLong(offset + CUSTOMER_ID_OFFSET);
    final long currentPrice = buffer.getLong(offset + PRICE_OFFSET);
    final boolean bidSucceed = 0 != buffer.getByte(offset + BID_SUCCEEDED_OFFSET);

    int length1 = buffer.getInt(CHANNEL_NAME_OFFSET);

    // Đọc và in ra channelName từ buffer
    byte[] channelNameRead = new byte[length1];
    buffer.getBytes(offset + CHANNEL_NAME_OFFSET, channelNameRead);
    System.out.println("Channel Name: " + new String(channelNameRead, StandardCharsets.UTF_8));


    lastBidSeen = currentPrice;

    printOutput("SessionMessage(" + clusterSessionId + ", " + correlationId + "," +
      customerId + ", " + currentPrice + ", " + bidSucceed + ")");
  }

  public void onSessionEvent(
    final long correlationId,
    final long clusterSessionId,
    final long leadershipTermId,
    final int leaderMemberId,
    final EventCode code,
    final String detail) {
    printOutput(
      "SessionEvent(" + correlationId + ", " + leadershipTermId + ", " +
        leaderMemberId + ", " + code + ", " + detail + ")");
  }

  public void onNewLeader(
    final long clusterSessionId,
    final long leadershipTermId,
    final int leaderMemberId,
    final String ingressEndpoints) {
    printOutput("NewLeader(" + clusterSessionId + ", " + leadershipTermId + ", " + leaderMemberId + ")");
  }

  private void bidInAuction(final AeronCluster aeronCluster) {
    long count = 0;
    while (!Thread.currentThread().isInterrupted()) {
      final long correlationId = sendBid(aeronCluster, count);
      printOutput("Sent(" + (correlationId) + ", " + customerId + ", " + count + ")");
      count++;

      idleStrategy.idle(aeronCluster.pollEgress());
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private long sendBid(final AeronCluster aeronCluster, final long price) {
    final long correlationId = this.correlationId++;
    actionBidBuffer.putLong(CORRELATION_ID_OFFSET, correlationId);
    actionBidBuffer.putLong(CUSTOMER_ID_OFFSET, 1997);
    actionBidBuffer.putLong(PRICE_OFFSET, 1998);

    byte[] channelNameBytes = "NGUYEN VAN A".getBytes(StandardCharsets.UTF_8);
    actionBidBuffer.putBytes(CHANNEL_NAME_OFFSET, channelNameBytes);

    idleStrategy.reset();
    while (aeronCluster.offer(actionBidBuffer, 0, BID_MESSAGE_LENGTH) < 0)
    {
      idleStrategy.idle(aeronCluster.pollEgress());
    }

    return correlationId;
  }

  public static String ingressEndpoints(final List<String> hostnames) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hostnames.size(); i++) {
      sb.append(i).append('=');
      sb.append(hostnames.get(i)).append(':').append(calculatePort(i, CLIENT_FACING_PORT_OFFSET));
      sb.append(',');
    }

    sb.setLength(sb.length() - 1);
    return sb.toString();
  }

  private void printOutput(final String message) {
    System.out.println(message);
  }

  public static void main(final String[] args) {
    final int customerId = Integer.parseInt(System.getProperty("aeron.cluster.tutorial.customerId"));       // <1>
    final String[] hostnames = new String[]{"172.16.0.5"};
    final String ingressEndpoints = ingressEndpoints(Arrays.asList(hostnames));
    System.out.println("ingressEndpoints..." + ingressEndpoints);

    final Producer client = new Producer(customerId);

    // tag::connect[]
    try (
      MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()// <1>
        .threadingMode(ThreadingMode.SHARED)
        .dirDeleteOnStart(true)
        .dirDeleteOnShutdown(true));

      AeronCluster aeronCluster = AeronCluster.connect(
        new AeronCluster.Context()
          .egressListener(client)
//                                .egressChannel("aeron:udp?endpoint=localhost:0")
          .egressChannel("aeron:udp?endpoint=239.255.255.1:4300|interface=172.16.0.8|ttl=16")// <3>
          .aeronDirectoryName(mediaDriver.aeronDirectoryName())
          .ingressChannel("aeron:udp")                                                                    // <4>
          .ingressEndpoints(ingressEndpoints)))                                                           // <5>
    {
      aeronCluster.leaderMemberId();
      // end::connect[]
      client.bidInAuction(aeronCluster);
    }
  }
}
