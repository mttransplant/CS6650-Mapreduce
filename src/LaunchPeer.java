public class LaunchPeer {
  public static void main(String[] args) {
    Peer peer = new Peer();
    peer.join();
    peer.leave();
  }
}