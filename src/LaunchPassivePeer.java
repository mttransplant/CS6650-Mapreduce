/**
 * a class to launch a Peer for demo purposes
 * when run(), submits a job, waits for the results, and then prints the results for display
 */
public class LaunchPassivePeer implements Runnable {
  private int secondsToWait;
  private Peer peer;

  public LaunchPassivePeer(int secondsToWait, int port) {
    this.secondsToWait = secondsToWait;
    this.peer = new Peer(port);
    this.peer.join();
  }

  @Override
  public void run() {
    try {
      Thread.sleep(this.secondsToWait * 1000);
    } catch (InterruptedException ie) {
      System.out.println("Thread interrupted; no cause for alarm...");
    }

    this.peer.leave();
  }
}