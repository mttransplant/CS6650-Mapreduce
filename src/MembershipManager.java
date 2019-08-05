import java.net.InetAddress;
import java.util.List;

class MembershipManager implements RemoteMembershipManager {
  List<Uuid> coordinators;

  @Override
  public Uuid generateUuid(InetAddress memberAddress) {
    return new Uuid(memberAddress);
  }

  @Override
  public RemoteCoordinator addMember(Uuid uuid) {
    return null;
  }

  @Override
  public boolean removeMember(Uuid uuid) {
    // TODO: if this uuid is a coordinator, manage its replacement
    return false;
  }
}