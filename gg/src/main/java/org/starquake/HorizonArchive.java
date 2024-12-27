package org.starquake;

import io.aeron.driver.MediaDriver;

import java.io.File;

import io.aeron.driver.ThreadingMode;
import io.aeron.archive.Archive;

public class HorizonArchive {
    public static void main(String[] args) {
        MediaDriver.Context driverContext = new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnShutdown(true)
                .dirDeleteOnStart(true);

        try (MediaDriver mediaDriver = MediaDriver.launch(driverContext)) {
            System.out.println("Embedded Media Driver started");

            Archive.Context archiveContext = new Archive.Context()
                    .aeronDirectoryName(driverContext.aeronDirectoryName())
                    .archiveDir(new File("/archive"))
                    .controlChannel("aeron:udp?endpoint=127.0.0.1:8010")
                    .localControlChannel("aeron:ipc")
                    .recordingEventsChannel("aeron:udp?endpoint=127.0.0.1:8020")
                    .replicationChannel("aeron:udp?endpoint=127.0.0.1:8030")
                    .recordingEventsEnabled(true);  //they should be enabled, default is false

            try (Archive archive = Archive.launch(archiveContext)) {
                System.out.println("Aeron Archive started");
                Thread.currentThread().join();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
