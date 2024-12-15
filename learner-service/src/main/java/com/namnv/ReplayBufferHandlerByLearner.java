package com.namnv;

import com.namnv.core.command.CommandHandler;
import com.namnv.core.replay.ReplayBufferEvent;
import com.namnv.core.replay.ReplayBufferHandler;
import com.namnv.core.snapshot.BaseCommandSnapshot;
import com.namnv.core.state.StateMachineManager;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ReplayBufferHandlerByLearner implements ReplayBufferHandler {
  private final CommandHandler commandHandler;
  private final StateMachineManager stateMachineManager;
  private final LearnerProperties learnerProperties;

  @Setter private int eventCount;
  private Instant lastSnapshot = Instant.now();

  @Override
  public void onEvent(ReplayBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
    if (!(event.getCommand() instanceof BaseCommandSnapshot)) {
      commandHandler.onCommand(event.getCommand());
    }
    eventCount--;
    if (endOfBatch && shouldSnapshot()) {
      stateMachineManager.takeSnapshot();
      resetAfterSnapshot();
    }
  }

  private boolean shouldSnapshot() {
    return eventCount < 0
        || Instant.now().compareTo(lastSnapshot.plus(learnerProperties.getSnapshotLifeTime())) > 0;
  }

  private void resetAfterSnapshot() {
    eventCount = learnerProperties.getSnapshotFragmentSize();
    lastSnapshot = Instant.now();
  }
}
