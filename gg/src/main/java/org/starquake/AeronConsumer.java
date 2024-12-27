package org.starquake;

import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.archive.client.AeronArchive;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.DirectBuffer;

public class AeronConsumer {
    //network interface and port to BIND to
    private static final String CHANNEL = "aeron:udp?endpoint=127.0.0.1:40456|control=127.0.0.1:40457";
    private static final int STREAM_ID = 10;
    private static final int FRAGMENT_LIMIT = 20;
    private static final IdleStrategy IDLE_STRATEGY = new BackoffIdleStrategy(1, 10, 1000, 1000);

    public static void main(String[] args) {
        MediaDriver.Context driverCtx = new MediaDriver.Context()
                .aeronDirectoryName("hhh")
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnShutdown(true)
                .dirDeleteOnStart(true);

        try (MediaDriver driver = MediaDriver.launch(driverCtx)) {
            System.out.println("Embedded Media Driver started");


            AeronArchive.Context archiveCtx = new AeronArchive.Context()
                    .controlRequestChannel("aeron:udp?endpoint=127.0.0.1:8010")
                    .controlResponseChannel("aeron:udp?endpoint=127.0.0.1:8020");


            try (AeronArchive aeronArchive = AeronArchive.connect(archiveCtx);
                 Subscription subscription = aeronArchive.context().aeron().addSubscription(CHANNEL, STREAM_ID)) {
                System.out.printf("Subscription ready on channel %s and stream %d%n", CHANNEL, STREAM_ID);

                FragmentAssembler fragmentAssembler = new FragmentAssembler(AeronConsumer::onMessage);

                while (true) {
                    int fragmentsRead = subscription.poll(fragmentAssembler, FRAGMENT_LIMIT);
                    if (fragmentsRead == 0) {
                        IDLE_STRATEGY.idle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static void onMessage(DirectBuffer buffer, int offset, int length, Header header) {
        String message = buffer.getStringWithoutLengthAscii(offset, length);
        System.out.println("Received message: " + message);
    }
}
