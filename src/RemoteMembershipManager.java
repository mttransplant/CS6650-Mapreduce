import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

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

/***
 * an interface to represent the manager of group membership for a peer-to-peer map/reduce service
 *
 * must maintain a list of references to all designated RemoteCoordinators,
 * adding and removing RemoteCoordinators as necessary
 *
 * is assumed to always be available at a published (universally known) IP address
 */
public interface RemoteMembershipManager extends Remote {
  int PORT = 1099;
  String serviceHost = "DEDICATED_IP"; // TODO: establish this

  /**
   * a method to generate a Uuid for a Peer
   * called by a new User
   *
   * @param memberAddress InetAddress of the User requesting a Uuid
   * @return a newly generated Uuid for the invoking new User
   */
  Uuid generateUuid(InetAddress memberAddress);

  /**
   * a method to register a new User with the network
   * called by a new User
   * designates this new User as a RemoteCoordinator if one is needed
   * delegates responsibility to a RemoteCoordinator
   *
   * @param uuid the Uuid of the new Peer
   * @return a reference to a RemoteCoordinator through which the invoking new User can submit jobs
   */
  RemoteCoordinator addMember(Uuid uuid);

  /**
   * a method to remove a User from the network
   * called by a User
   *
   * if this User is a RemoteCoordinator, removes this User from its list of RemoteCoordinators
   * and then delegates the designation of another RemoteCoordinator to a RemoteCoordinator
   *
   * if this User is not a RemoteCoordinator, delegates responsibility of removal to a RemoteCoordinator
   *
   * @param uuid the Uuid of the User to be removed
   * @return true of the removal was successful, false otherwise
   */
  boolean removeMember(Uuid uuid);
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
  List<RemoteTaskManager> getWorkers(Job job); // called from a JobManager, gets references to RemoteWorkers
}

/**
 * must maintain separate lists of references to all RemoteCoordinators and each of its backup RemoteJobManagers
 * all "replica" JobManagers for a Job should proceed randomly through the job and report interim results back to its fellows (for redundancy)
 */
interface RemoteJobManager extends Remote {
  JobResult manageJob(Job job); // called from a Coordinator, initiates task execution, delegates back to RemoteCoordinator for RemoteTaskManager allocation
}

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
interface RemoteTaskManager extends Remote {
  TaskResult performTask(Task task); // called from a JobManager
}

interface Job {}  // TODO: should have Uuid of submitting User as field
interface Task {}
interface JobResult {}
interface TaskResult {}

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

class Peer implements User, RemoteCoordinator, RemoteJobManager, RemoteTaskManager {
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
  public boolean submitJob(JobId jobId) {
    return false;
  }

  @Override
  public List<RemoteTaskManager> getWorkers(Job job) {
    return null;
  }

  @Override
  public boolean leave() {
    return this.service.removeMember(this.uuid);
  }

  @Override
  public JobResult receiveResults(JobResult results) {
    return null;
  }

  @Override
  public JobResult manageJob(Job job) {
    return null;
  }

  @Override
  public TaskResult performTask(Task task) {
    return null;
  }
}