package namnv.consumer;

import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.client.RecordingDescriptorConsumer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class RecordingReplayer {
  private static final String AERON_UDP_ENDPOINT = "aeron:udp?endpoint=";
  private static final String ARCHIVE_HOST = "172.16.0.2";
  private static final String THIS_HOST = "172.16.0.10";

  private static final int ARCHIVE_CONTROL_PORT = 8010;
  private static final int ARCHIVE_EVENT_PORT = 8020;

  private static final String RECORDING_CHANNEL = "aeron:udp";

  //Log Stream: 100
  private static final int STREAM_ID = 10;

  private static final IdleStrategy IDLE_STRATEGY = new BackoffIdleStrategy();

  private static void replayRecording(AeronArchive archive, Recording recording) {
    System.out.printf("Replaying recording: ID=%d from position %d to %d%n", recording.recordingId, recording.startPosition, recording.stopPosition);

    int replayStreamId = STREAM_ID + 1;
    long length = recording.stopPosition - recording.startPosition;

    final var localReplayChannelEphemeral = AERON_UDP_ENDPOINT + THIS_HOST + ":0";

    // create subscription for replay
    try (Subscription subscription = archive.context().aeron().addSubscription(localReplayChannelEphemeral, replayStreamId)) {
      startReplay(archive,
        recording.recordingId,
        recording.startPosition,
        length,
        subscription.tryResolveChannelEndpointPort(),
        replayStreamId,
        subscription);
    }
  }

  private static void startReplay(AeronArchive archive,
                                  long recordingId,
                                  long position,
                                  long length,
                                  String replayChannel,
                                  int replayStreamId,
                                  Subscription subscription) {
    // start replay
    var replaySession = archive.startReplay(recordingId, position, length, replayChannel, replayStreamId);

    var fragmentAssembler = new FragmentAssembler((buffer, offset, bufferLength, header) -> {
      var message = buffer.getStringWithoutLengthAscii(offset, bufferLength);
      System.out.println("Replayed message: " + message + " position: " + header.position());
    });

    while (true) {
      //fragment limit is set low to allow us to consume them one by one
      int fragmentsRead = subscription.poll(fragmentAssembler, 1);
      if (fragmentsRead > 0) {
        System.out.println("Read " + fragmentsRead + " fragments");
//        archive.stopReplay(replaySession);
      }

      IDLE_STRATEGY.idle();
    }
  }


  public static void main(String[] args) throws InterruptedException {
    MediaDriver.Context driverCtx = new MediaDriver.Context()
      .threadingMode(ThreadingMode.SHARED)
      .dirDeleteOnShutdown(true)
      .dirDeleteOnStart(true);

    try (MediaDriver driver = MediaDriver.launch(driverCtx)) {
      System.out.println("Recording Replayer Media Driver started");

      var archiveCtx = new AeronArchive.Context()
        .controlRequestChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_CONTROL_PORT)
        .recordingEventsChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_EVENT_PORT)
        .controlResponseChannel(AERON_UDP_ENDPOINT + THIS_HOST + ":0");

      try (var archive = AeronArchive.connect(archiveCtx)) {
        System.out.println("Connected to Archive");
        replayRecording(archive);
      }
    }
  }

  private static void replayRecording(AeronArchive archive) {
    var latestRecording = findLatestRecording(archive);
    if (latestRecording != null) {
      replayRecording(archive, latestRecording);
    } else {
      replayRecording(archive);
    }
  }

  private static Recording findLatestRecording(AeronArchive archive) {
    var latestRecording = new Recording[1];
    RecordingDescriptorConsumer consumer = (controlSessionId, correlationId, recordingId,
                                            startTimestamp, stopTimestamp, startPosition,
                                            stopPosition, initialTermId, segmentFileLength,
                                            termBufferLength, mtuLength, sessionId,
                                            streamId, strippedChannel, originalChannel,
                                            sourceIdentity) -> {
      if (latestRecording[0] == null || recordingId > latestRecording[0].recordingId) {
        System.out.printf("Found matching recording: ID=%d, channel=%s, stream=%d%n, startPos=%d%n, stopPos=%d%n", recordingId, strippedChannel, streamId, startPosition, stopPosition);
        latestRecording[0] = new Recording(recordingId, startPosition, stopPosition, streamId, strippedChannel);
      }
    };
    // First list all recordings to see what's available
    archive.listRecordingsForUri(
      0,    // from record id
      Integer.MAX_VALUE,  // max number of records
      RECORDING_CHANNEL,  // specific channel we want
      STREAM_ID, consumer);

    return latestRecording[0];
  }

  private record Recording(long recordingId, long startPosition, long stopPosition, int streamId, String channel) {
  }
}
