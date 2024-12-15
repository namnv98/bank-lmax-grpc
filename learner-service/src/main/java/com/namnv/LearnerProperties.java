package com.namnv;

import java.time.Duration;
import lombok.Data;

@Data
public class LearnerProperties {
    private int bufferSize = 1 << 5;
    private int pollingInterval = 100;
    private int maxSnapshotCheckCircles = 50;
    private int snapshotFragmentSize = 10_000;
    private Duration snapshotLifeTime = Duration.ofSeconds(5);
}
