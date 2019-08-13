import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

/**
 * a class to universally identify a Peer by a uuid, inetAddress, and port used for communication
 */
public class Uuid implements Serializable {
  private final InetAddress inetAddress;
  private final int clientPort;
  private final String uuid;

  // default initializer
  public Uuid(InetAddress inetAddress, int clientPort) {
    this.inetAddress = inetAddress;
    this.clientPort = clientPort;
    this.uuid = UUID.randomUUID().toString();
  }

  // method to return the IP address of the peer for this Uuid
  public InetAddress getAddress() {
    return this.inetAddress;
  }

  // method to return the client port of the peer for this Uuid
  public int getClientPort() {
    return this.clientPort;
  }

  // method to return the string of the unique identifier assigned to this Uuid
  @Override
  public String toString() {
    return this.uuid;
  }
}
