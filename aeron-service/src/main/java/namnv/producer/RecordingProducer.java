package namnv.producer;

import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.UnsafeBuffer;

public class RecordingProducer {
    // Change from endpoint to explicit publish
    private static final String RECORDING_CHANNEL =
            "aeron:udp?control-mode=dynamic|control=172.16.0.11:40456";
    private static final int MESSAGE_LENGTH = 32;
    private static final int STREAM_ID = 10;

    public static void main(String[] args) {
        String archiveDirPath = System.getProperty("archive.dir", System.getProperty("user.dir") + "/aeron-service/producer");

        MediaDriver.Context driverCtx = new MediaDriver.Context()
//                .aeronDirectoryName(archiveDirPath)
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnShutdown(true)
                .dirDeleteOnStart(true);

        try (MediaDriver driver = MediaDriver.launch(driverCtx)) {
            System.out.println("Recording Producer Media Driver started");

            // Connect to Archive
            AeronArchive.Context archiveCtx = new AeronArchive.Context()
                    .controlRequestChannel("aeron:udp?endpoint=172.16.0.5:9001")
                    .controlResponseChannel("aeron:udp?endpoint=172.16.0.11:8020");

            try (AeronArchive aeronArchive = AeronArchive.connect(archiveCtx)) {
                System.out.println("Connected to Archive");
                long recordingId = aeronArchive.startRecording(RECORDING_CHANNEL, STREAM_ID, SourceLocation.REMOTE);
                System.out.println("Started recording with ID: " + recordingId);

                // Create publication
                try (Publication publication = aeronArchive.context().aeron().addPublication(
                        RECORDING_CHANNEL, STREAM_ID)) {

                    System.out.println("Publication created, waiting for connection...");
                    while (!publication.isConnected()) {
                        Thread.sleep(100);
                        System.out.println("Waiting for publication connection...");
                    }

                    for (int i = 0; i < 100; i++) {
                        UnsafeBuffer buffer = new UnsafeBuffer(new byte[MESSAGE_LENGTH]);
                        String message = "Hello, Recorded World!";
                        buffer.putStringWithoutLengthAscii(0, message + i);


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
                            Thread.sleep(10);
                        }

                        if (result > 0) {
                            System.out.println("Message successfully published: " + message);
                        } else {
                            System.out.println("Failed to publish message");
                            System.exit(1);
                        }
                        Thread.sleep(2000);
                    }
                    while (true) {
                        System.out.println("Producer healthy and standing by...");
                        Thread.sleep(5000);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
