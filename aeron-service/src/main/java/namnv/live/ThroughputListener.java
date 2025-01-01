package namnv.live;


public interface ThroughputListener extends AutoCloseable {

  /**
   * Called for a rate report.
   *
   * @param context reporter context
   * @param messages number of messages
   */
  void onReport(ThroughputReporter.Context context, double messages);
}
