import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

public class Uuid implements Serializable {
  private final InetAddress inetAddress;
  private final int clientPort;
  private final String uuid;

  public Uuid(InetAddress inetAddress, int clientPort) {
    this.inetAddress = inetAddress;
    this.clientPort = clientPort;
    this.uuid = UUID.randomUUID().toString();
  }

  public InetAddress getAddress() {
    return this.inetAddress;
  }

  public int getClientPort() {
    return this.clientPort;
  }

  @Override
  public String toString() {
    return this.uuid;
  }
}
