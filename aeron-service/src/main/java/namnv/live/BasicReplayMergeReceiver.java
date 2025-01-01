package namnv.live;


import io.aeron.*;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.client.ReplayMerge;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MediaDriver.Context;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.CloseHelper;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.aeron.CommonContext.MDC_CONTROL_MODE_MANUAL;
import static io.aeron.CommonContext.UDP_MEDIA;
import static namnv.live.AeronHelper.printAsciiMessage;
import static namnv.live.AeronHelper.printSubscription;

public class BasicReplayMergeReceiver {
    private static final String AERON_UDP_ENDPOINT = "aeron:udp?endpoint=";
    private static final String ARCHIVE_HOST = "172.16.0.2";
    private static final String THIS_HOST = "172.16.0.10";
    private static final int ARCHIVE_CONTROL_PORT = 8010;
    private static final int ARCHIVE_EVENT_PORT = 8020;

    //Log Stream: 100
    private static final int STREAM_ID = 1001;
    
    private static final int FRAGMENT_LIMIT = 1;

    private static MediaDriver mediaDriver;
    private static Aeron aeron;
    private static AeronArchive aeronArchive;
    private static final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Main runner.
     *
     * @param args args
     */
    public static void main(String[] args) throws InterruptedException {
        SigInt.register(BasicReplayMergeReceiver::close);
        Path aeronPath = Paths.get(CommonContext.generateRandomDirName());

        mediaDriver =
                MediaDriver.launch(
                        new Context().aeronDirectoryName(aeronPath.toString()).spiesSimulateConnection(true));

        aeron =
                Aeron.connect(
                        new Aeron.Context()
                                .aeronDirectoryName(aeronPath.toString())
                                .availableImageHandler(AeronHelper::printAvailableImage)
                                .unavailableImageHandler(AeronHelper::printUnavailableImage));

        aeronArchive =
                AeronArchive.connect(
                        new AeronArchive.Context()
                                .aeron(aeron)
                                .controlRequestChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_CONTROL_PORT)
                                .recordingEventsChannel(AERON_UDP_ENDPOINT + ARCHIVE_HOST + ":" + ARCHIVE_EVENT_PORT)
                                .controlResponseChannel(AERON_UDP_ENDPOINT + THIS_HOST + ":0"));

        String aeronDirectoryName = mediaDriver.aeronDirectoryName();
        System.out.printf("### aeronDirectoryName: %s%n", aeronDirectoryName);

        long controlSessionId = aeronArchive.controlSessionId();
        System.out.printf("### controlSessionId: %s%n", controlSessionId);

        RecordingDescriptor rd =
                AeronArchiveUtil.findLastRecording(aeronArchive, rd1 -> rd1.streamId == STREAM_ID);
        if (rd == null) {
            throw new NoSuchElementException("recording not found");
        }
        System.out.println(rd.streamId);

        // mds subscription
        Subscription subscription =
                aeron.addSubscription(
                        new ChannelUriStringBuilder()
                                .media(UDP_MEDIA)
                                .controlMode(MDC_CONTROL_MODE_MANUAL)
                                .build(),
                        STREAM_ID);

        printSubscription(subscription);

        String replayChannel =
                new ChannelUriStringBuilder()
                        .media(UDP_MEDIA)
                        .sessionId(rd.sessionId)
                        .build();

        String replayDestination =
                new ChannelUriStringBuilder()
                        .media(UDP_MEDIA)
                        .endpoint(THIS_HOST + ":0")
                        .build();

        String liveDestination =
                new ChannelUriStringBuilder()
                        .media(UDP_MEDIA)
                        .controlEndpoint(ARCHIVE_HOST + ":" + ARCHIVE_CONTROL_PORT)
                        .endpoint(THIS_HOST + ":0")
                        .build();

        ReplayMerge replayMerge =
                new ReplayMerge(
                        subscription,
                        aeronArchive,
                        replayChannel,
                        replayDestination,
                        liveDestination,
                        rd.recordingId,
                        rd.startPosition);

        final FragmentHandler fragmentHandler = printAsciiMessage(STREAM_ID);
        FragmentAssembler fragmentAssembler = new FragmentAssembler(fragmentHandler);
        SleepingMillisIdleStrategy idleStrategy = new SleepingMillisIdleStrategy(300);

        while (running.get()) {
            int progress = 0;
            progress += pollReplayMerge(replayMerge, fragmentHandler, fragmentAssembler);
            idleStrategy.idle(progress);
        }

        System.out.println("Shutting down...");
        close();
    }

    private static int pollReplayMerge(
            ReplayMerge replayMerge,
            FragmentHandler fragmentHandler,
            FragmentAssembler fragmentAssembler) {

        int progress;

        if (replayMerge.isMerged()) {
            final Image image = replayMerge.image();
            progress = image.poll(fragmentAssembler, FRAGMENT_LIMIT);

            if (image.isClosed()) {
                System.err.println("### replayMerge.image is closed, exiting");
                throw new RuntimeException("good bye");
            }
        } else {
            progress = replayMerge.poll(fragmentHandler, FRAGMENT_LIMIT);

            if (replayMerge.hasFailed()) {
                System.err.println("### replayMerge has failed, exiting");
                throw new RuntimeException("good bye");
            }
        }

        return progress;
    }

    static void close() {
        running.set(false);
        CloseHelper.close(aeronArchive);
        CloseHelper.close(aeron);
        CloseHelper.close(mediaDriver);
    }
}