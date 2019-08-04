import java.net.InetAddress;
import java.util.UUID;

class Uuid {
  private final InetAddress inetAddress;
  private final String uuid;

  public Uuid(InetAddress inetAddress) {
    this.inetAddress = inetAddress;
    this.uuid = UUID.randomUUID().toString();
  }

  public InetAddress getAddress() {
    return this.inetAddress;
  }

  @Override
  public String toString() {
    return this.uuid;
  }
}
