package namnv.cluster;

import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.UnsafeBuffer;

public class RecordingProducer {
  private static final String AERON_UDP_ENDPOINT = "aeron:udp?endpoint=";
  private static final String ARCHIVE_HOST = "172.16.0.2";
  private static final String THIS_HOST = "172.16.0.5";

  private static final int ARCHIVE_CONTROL_PORT = 8010;
  private static final int ARCHIVE_EVENT_PORT = 8020;

  // Change from endpoint to explicit publish
  private static final String RECORDING_CHANNEL = "aeron:udp?control-mode=dynamic|control=172.16.0.5:40456";
  private static final int MESSAGE_LENGTH = 32;
  private static final int STREAM_ID = 10;


  private Publication publication;

  public RecordingProducer() {
    var archiveDirPath = System.getProperty("user.dir") + "/recording_producer";

    var driverCtx = new MediaDriver.Context()
      .aeronDirectoryName(archiveDirPath)
      .threadingMode(ThreadingMode.SHARED)
      .dirDeleteOnShutdown(true)
      .dirDeleteOnStart(true);

    MediaDriver.launch(driverCtx);
    System.out.println("Recording Producer Media Driver started");

    // Connect to Archive
    var archiveCtx = new AeronArchive.Context()
      .aeronDirectoryName(archiveDirPath)
      .controlRequestChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_CONTROL_PORT)
      .recordingEventsChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_EVENT_PORT)
      .controlResponseChannel(AERON_UDP_ENDPOINT + THIS_HOST + ":0");

    var aeronArchive = AeronArchive.connect(archiveCtx);
    System.out.println("Connected to Archive");
    long recordingId = aeronArchive.startRecording(RECORDING_CHANNEL, STREAM_ID, SourceLocation.REMOTE);
    System.out.println("Started recording with ID: " + recordingId);

    // Create publication
    publication = aeronArchive.context().aeron().addPublication(RECORDING_CHANNEL, STREAM_ID);

    System.out.println("Publication created, waiting for connection...");
    while (!publication.isConnected()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      System.out.println("Waiting for publication connection...");
    }
  }

  public boolean save(String message) {
    var buffer = new UnsafeBuffer(new byte[MESSAGE_LENGTH]);
    buffer.putStringWithoutLengthAscii(0, message);

    System.out.println("Attempting to publish message...");
    long result;
    while ((result = publication.offer(buffer, 0, MESSAGE_LENGTH)) < 0L) {
      if (result == Publication.BACK_PRESSURED) {
        System.out.println("Back pressured");
      } else if (result == Publication.NOT_CONNECTED) {
        System.out.println("Not connected");
      } else if (result == Publication.ADMIN_ACTION) {
        System.out.println("Admin action");
      } else if (result == Publication.CLOSED) {
        System.out.println("Publication closed");
        break;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (result > 0) {
      System.out.println("Message successfully published: " + message);
      return true;
    }
    System.out.println("Failed to publish message");
    return false;
  }
}

