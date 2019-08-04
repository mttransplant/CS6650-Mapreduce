import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.UUID;

/*
 * 1. establish MembershipManager for service
 * 2. new User reaches out to MembershipManager to join service (1st and then 1 out of every x Peers are assigned as Coordinators)
 * 3. MembershipManager informs new User a live Coordinator
 * 4. User submits Job to Coordinator
 * 5. Coordinator selects JobManagers (Coordinator can't be JobManager or Worker)
 * 6. JobManagers complete Job randomly (for redundancy)... updating each other as they go
 * 7. JobManagers responsible for designating a new JobManager if a former JobManager dies
 * 8. JobManager requests Workers from a Coordinator (periodically if necessary)
 * 9. JobManager coordinates Tasks work with Workers
 * 10. JobManager delivers Results to User that submitted the Job
 * 11. User leaves service
 * 12. MembershipManager removes User (replaces with new Coordinator if it was a Coordinator)
 */

/**
 * must maintain a reference to the RemoteMembershipManager
 * must maintain a list of references to all RemoteCoordinators
 * if it encounters a dead RemoteCoordinator, it must leave() and then join() again
 */
interface User extends RemoteUser {
  RemoteCoordinator join(); // delegates to the RemoteMembershipManager
  boolean submitJob(Job job); // delegates to the RemoteCoordinator
  boolean leave(); // delegates to the RemoteMembershipManager
}

interface RemoteUser extends Remote {
  Results receiveResults(Results results); // called from the JobManager
}

/***
 * must maintain a list of references to all RemoteCoordinators
 */
public interface RemoteMembershipManager extends Remote {
  int PORT = 1099;
  String serviceHost = "DEDICATED_IP"; // TODO: establish this

  Uuid generateUuid(InetAddress memberAddress); // called from a User
  RemoteCoordinator addMember(Uuid uuid);  // called from a User, delegate to a RemoteCoordinator, assigns new User as RemoteCoordinator if appropriate
  boolean removeMember(Uuid uuid);  // called from a User, delegate to a RemoteCoordinator, assigns a replacement RemoteCoordinator if appropriate
}

/***
 * must maintain a list of Uuids for all non-Coordinator Peers
 * must enforce load-balancing policy
 */
interface RemoteCoordinator extends Remote {
  boolean addPeer(Uuid peer); // called from the MembershipManager, adds Uuid to its list of active Peers
  boolean removePeer(Uuid peer); // called from the MembershipManager, removes Uuid from its list of active Peers
  List<Uuid> getActivePeers(); // called from the MembershipManager, to be used to bring new Coordinators online
  boolean setActivePeers(List<Uuid> peers); // called from the MembershipManager, to be used to bring new Coordinators online
  boolean submitJob(Job job); // called from a User, gets references (and passes Job) to RemoteJobManagers
  List<RemoteWorker> getWorkers(Job job); // called from a JobManager, gets references to RemoteWorkers
}

/**
 * must maintain separate lists of references to all RemoteCoordinators and each of its backup RemoteJobManagers
 * all "replica" JobManagers for a Job should proceed randomly through the job and report interim results back to its fellows (for redundancy)
 */
interface RemoteJobManager extends Remote {
  Results manageJob(Job job); // called from a Coordinator, initiates task execution, delegates back to RemoteCoordinator for RemoteWorker allocation
}

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
interface RemoteWorker extends Remote {
  IntermediateResult performTask(Task task); // called from a JobManager
}

interface Job {}  // TODO: should have Uuid of submitting User as field
interface Task {}
interface Results {}
interface IntermediateResult {}

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

class Peer implements User, RemoteCoordinator, RemoteJobManager, RemoteWorker {
  private RemoteMembershipManager service;
  private Uuid uuid;

  public Peer() {
    try {
      Registry registry = LocateRegistry.getRegistry(RemoteMembershipManager.serviceHost, RemoteMembershipManager.PORT);
      this.service = (RemoteMembershipManager) registry.lookup(RemoteMembershipManager.serviceHost);
      this.uuid = this.service.generateUuid(InetAddress.getLocalHost());
    } catch (UnknownHostException uhe) {
      // TODO: handle this exception
    } catch (RemoteException re) {
      // TODO: then handle this exception
    } catch (NotBoundException nbe) {
      // TODO: then handle this exception too
    }
  }

  @Override
  public RemoteCoordinator join() {
    return this.service.addMember(this.uuid);
  }

  @Override
  public boolean addPeer(Uuid peer) {
    return false;
  }

  @Override
  public boolean removePeer(Uuid peer) {
    return false;
  }

  @Override
  public List<Uuid> getActivePeers() {
    return null;
  }

  @Override
  public boolean setActivePeers(List<Uuid> peers) {
    return false;
  }

  @Override
  public boolean submitJob(Job job) {
    return false;
  }

  @Override
  public List<RemoteWorker> getWorkers(Job job) {
    return null;
  }

  @Override
  public boolean leave() {
    return this.service.removeMember(this.uuid);
  }

  @Override
  public Results receiveResults(Results results) {
    return null;
  }

  @Override
  public Results manageJob(Job job) {
    return null;
  }

  @Override
  public IntermediateResult performTask(Task task) {
    return null;
  }
}