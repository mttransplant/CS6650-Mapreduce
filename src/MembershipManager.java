import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * a class to represent...
 */
class MembershipManager implements RemoteMembershipManager {
  private final List<RemoteCoordinator> coordinators;
  private final Random randomNumberGenerator;
  private int memberCount;

  public MembershipManager() {
    this.coordinators = new LinkedList<>();
    this.randomNumberGenerator = new Random();
    this.memberCount = 0;
  }

  @Override
  public Uuid generateUuid(InetAddress memberAddress) {
    return new Uuid(memberAddress);
  }

  @Override
  public Uuid addMember(Uuid newMember) {
    incrementMemberCount();

    // new member can be either a Coordinator or a non-Coordinator
    if (newCoordinatorRequired()) {
      addCoordinator(newMember);
    } else {
      for (RemoteCoordinator rc : coordinators) {
        rc.addPeer(newMember);
      }
    }

    return getCoordinator();
  }

  @Override
  public boolean removeMember(Uuid oldMember) {
    boolean result = true;

    decrementMemberCount();

    if (isCoordinator(oldMember)) {
      result = removeCoordinator(oldMember);
    } else {
      for (RemoteCoordinator rc : coordinators) {
        if (!rc.removePeer(oldMember)) {
          result = false;
        }
      }
    }

    if (newCoordinatorRequired()) {
      addCoordinator();
    } else if (tooManyCoordinators()) {
      removeCoordinator(getCoordinator());
    }

    return result;
  }

  /**
   * a method to enforce the Coordinator to JobManager/TaskManager load-balancing policy
   * must handle two scenarios:
   * 1. a new member is added... must add a new Coordinator
   * 2. an old Coordinator is removed... should it be replaced?
   *
   * @return true if load-balancing policy dictates, false otherwise
   */
  private boolean newCoordinatorRequired() {
    return false;
  }

  private boolean tooManyCoordinators() {
    return false;
  }

  private void addCoordinator(Uuid uuid) {

  }

  private void addCoordinator() {

  }

  private boolean removeCoordinator(Uuid uuid) {
    return false;
  }

  private boolean isCoordinator(Uuid uuid) {
    return false;
  }

  private Uuid getCoordinator() {
    int numCoordinators;
    int index;
    RemoteCoordinator coordinator;

    synchronized (coordinators) {
      numCoordinators = coordinators.size();

      synchronized (randomNumberGenerator) {
        index = randomNumberGenerator.nextInt(numCoordinators);
      }

      coordinator = coordinators.get(index);
    }

    return coordinator.getUuid();
  }

  private synchronized void incrementMemberCount() {
    this.memberCount += 1;
  }

  private synchronized void decrementMemberCount() {
    this.memberCount -= 1;
  }
}