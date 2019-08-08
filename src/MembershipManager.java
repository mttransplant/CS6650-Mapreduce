import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * a class that managers membership in a peer-to-peer map/reduce service
 * enforcing load-balancing policies for how man Coordinators are needed
 */
public class MembershipManager implements RemoteMembershipManager, Communicate {
  public static final int PORT = 1099;
  public static final String SERVICE_HOST = "127.0.0.1"; // TODO: establish this
  public static final String SERVICE_NAME = "MEMBERSHIP_MANAGER";

  private final List<RemoteCoordinator> coordinators;
  private final Random randomNumberGenerator;
  private int memberCount;
  private final int PEERS_PER_COORDINATOR = 10;
  final static String USER = "user";
  final static String COORDINATOR = "coordinator";
  final static String JOB_MANAGER = "job_manager";
  final static String TASK_MANAGER = "task_manager";
  final static long MIN_MEMORY = 2000000;
  final static long MIN_PROCESSORS = 2;

  public MembershipManager() {
    this.coordinators = new LinkedList<>();
    this.randomNumberGenerator = new Random();
    this.memberCount = 0;
  }

  /* ---------- RemoteMembershipManager methods ---------- */

  @Override
  public Uuid generateUuid(InetAddress memberAddress) {
    return new Uuid(memberAddress);
  }

  @Override
  public Uuid addMember(Uuid newMember) throws RemoteException, NotBoundException {
    RemoteUser newUser = (RemoteUser) getRemoteRef(newMember, MembershipManager.USER);

    if (!newUser.hasMinimumResources()) {
      // TODO: do something here to notify user its join has failed.
      return null;
    }

    incrementMemberCount();

    // new member can be either a Coordinator or a non-Coordinator
    if (newCoordinatorRequired()) {
      designateNewPeerAsCoordinator(newMember);
    } else {
      for (RemoteCoordinator rc : this.coordinators) {
        rc.addPeer(newMember);
      }
    }

    return getCoordinatorUuid();
  }

  @Override
  public void removeMember(Uuid oldMember) throws RemoteException, NotBoundException {
    decrementMemberCount();
    int index;

    synchronized (this.coordinators) {
      index = getIndexOfCoordinator(oldMember);

      if (index >= 0) {
        this.coordinators.remove(index);
      } else {
        for (RemoteCoordinator rc : this.coordinators) {
          rc.removePeer(oldMember);
        }
      }
    }

    if (newCoordinatorRequired()) {
      selectPreExistingPeerToBeCoordinator();
    } else if (tooManyCoordinators()) {
      removeACoordinator();
    }
  }

  /* ---------- Communicate methods ---------- */

  @Override
  public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), MembershipManager.PORT);
    return registry.lookup(uuid.toString() + peerRole);
  }

  /* ---------- private helper methods --------- */

  /**
   * a method to determine if a new Coordinator should be designated
   * enforces a load-balancing policy
   *
   * @return true if load-balancing policy dictates, false otherwise
   */
  private boolean newCoordinatorRequired() {
    return this.coordinators.isEmpty() || this.coordinators.size() <= this.memberCount/PEERS_PER_COORDINATOR;
  }

  /**
   * a method to determine if a Coordinator should be de-selected
   * enforces a load-balancing policy
   *
   * @return true if load-balancing policy dictates, false otherwise
   */
  private boolean tooManyCoordinators() {
    return !this.coordinators.isEmpty() && this.coordinators.size() > this.memberCount/PEERS_PER_COORDINATOR;
  }

  /**
   * a method that designates the given Peer as a Coordinator
   */
  private void designateNewPeerAsCoordinator(Uuid uuid) throws RemoteException, NotBoundException {
    RemoteUser newUser = (RemoteUser) getRemoteRef(uuid, USER);
    newUser.bindCoordinator();
    RemoteCoordinator newCoordinator = (RemoteCoordinator) getRemoteRef(uuid, COORDINATOR);

    synchronized (this.coordinators) {
      if (!this.coordinators.isEmpty()) {
        RemoteCoordinator coordinator = getCoordinatorRef();
        newCoordinator.setActivePeers(coordinator.getActivePeers());
      }

      this.coordinators.add(newCoordinator);
    }
  }

  /**
   * a method to designate a pre-existing non-Coordinator Peer as a new Coordinator
   */
  private void selectPreExistingPeerToBeCoordinator() throws RemoteException, NotBoundException {
    RemoteCoordinator coordinator = getCoordinatorRef();
    Uuid peer = coordinator.getActivePeer();
    RemoteUser newUser = (RemoteUser) getRemoteRef(peer, USER);
    newUser.bindCoordinator();

    for (RemoteCoordinator rc : this.coordinators) {
      rc.removePeer(peer);
    }

    RemoteCoordinator newCoordinator = (RemoteCoordinator) getRemoteRef(peer, COORDINATOR);
    newCoordinator.setActivePeers(coordinator.getActivePeers());

    this.coordinators.add(newCoordinator);
  }

  /**
   * a method that randomly removes a Coordinator from the MembershipManager's list of Coordinators
   * and registers that Peer as available for non-Coordinator work with all remaining Coordinators
   */
  private void removeACoordinator() throws RemoteException, NotBoundException {
    RemoteCoordinator oldCoordinator;

    synchronized (this.coordinators) {
      int numCoordinators = this.coordinators.size();
      int index = this.randomNumberGenerator.nextInt(numCoordinators);
      oldCoordinator = this.coordinators.get(index);
      this.coordinators.remove(index);
    }

    Uuid newPeer = oldCoordinator.getUuid();
    RemoteUser newUser = (RemoteUser) getRemoteRef(newPeer, USER);
    newUser.unbindCoordinator();

    for (RemoteCoordinator rc : this.coordinators) {
      rc.addPeer(newPeer);
    }
  }

  /**
   * a method to get the Uuid of a Coordinator from the MembershipManager's list of Coordinators
   *
   * @return the Uuid of a Coordinator
   */
  private Uuid getCoordinatorUuid() throws RemoteException{
    return getCoordinatorRef().getUuid();
  }

  /**
   * a method to get a reference to a RemoteCoordinator from the MembershipManager's list of Coordinators
   *
   * @return a reference to a RemoteCoordinator
   */
  private RemoteCoordinator getCoordinatorRef() {
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

    return coordinator;
  }

  /**
   * a method that gets the index of a Coordinator in the MembershipManager's list of Coordinators by Uuuid
   *
   * @param uuid the Uuid of the Peer whose Coordinator index will be gotten, if it exists
   * @return the index of the given Peer in the list of Coordinators or -1 if the given Peer is not a Coordinator
   */
  private synchronized int getIndexOfCoordinator(Uuid uuid) throws RemoteException {
    int index = -1;

    for (int i = 0; i < this.coordinators.size(); i++) {
      String coordUuid = this.coordinators.get(i).getUuid().toString();

      if (coordUuid.equals(uuid.toString())) {
        index = i;
        break;
      }
    }

    return index;
  }

  private synchronized void incrementMemberCount() {
    this.memberCount += 1;
  }

  private synchronized void decrementMemberCount() {
    this.memberCount -= 1;
  }
}