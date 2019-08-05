import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Peer implements User, RemoteUser, Coordinator, RemoteCoordinator, JobManager, RemoteJobManager, TaskManager, RemoteTaskManager {
  private RemoteMembershipManager service;
  private Uuid uuid;
  private RemoteCoordinator coordinator;
  private final Map<String, Uuid> availablePeers;

  public Peer() {
    this.availablePeers = new HashMap<>();

    try {
      // connect to the MembershipService and get a Uuid
      Registry remoteRegistry = LocateRegistry.getRegistry(RemoteMembershipManager.serviceHost, RemoteMembershipManager.PORT);
      this.service = (RemoteMembershipManager) remoteRegistry.lookup(RemoteMembershipManager.serviceHost);
      this.uuid = this.service.generateUuid(InetAddress.getLocalHost());

      // create references to all Remote Peer interfaces
      RemoteUser user = this;
      RemoteCoordinator coordinator = this;
      RemoteJobManager jobManager = this;
      RemoteTaskManager taskManager = this;

      // get a stub for each of these Remote Peer interfaces
      RemoteUser userStub = (RemoteUser) UnicastRemoteObject.exportObject(user, RemoteMembershipManager.PORT);
      RemoteCoordinator coordinatorStub = (RemoteCoordinator) UnicastRemoteObject.exportObject(coordinator, RemoteMembershipManager.PORT);
      RemoteJobManager jobManagerStub = (RemoteJobManager) UnicastRemoteObject.exportObject(jobManager, RemoteMembershipManager.PORT);
      RemoteTaskManager taskManagerStub = (RemoteTaskManager) UnicastRemoteObject.exportObject(taskManager, RemoteMembershipManager.PORT);

      // create a local registry... or simply get it if it already exists
      Registry localRegistry;

      try {
        localRegistry = LocateRegistry.createRegistry(RemoteMembershipManager.PORT);
      } catch (RemoteException re) {
        localRegistry = LocateRegistry.getRegistry(RemoteMembershipManager.PORT);
      }

      // register this Peer as a RemoteUser, RemoteCoordinator, RemoteJobManager, and RemoteTaskManager
      localRegistry.rebind(getUuid().toString() + MembershipManager.USER, userStub);
      localRegistry.rebind(getUuid().toString() + MembershipManager.COORDINATOR, coordinatorStub);
      localRegistry.rebind(getUuid().toString() + MembershipManager.JOB_MANAGER, jobManagerStub);
      localRegistry.rebind(getUuid().toString() + MembershipManager.TASK_MANAGER, taskManagerStub);
    } catch (UnknownHostException uhe) {
      // TODO: handle this exception
    } catch (RemoteException re) {
      // TODO: then handle this exception
    } catch (NotBoundException nbe) {
      // TODO: then handle this exception too
    }
  }

  /* ---------- User methods ---------- */

  @Override
  public void join() {
    this.coordinator = (RemoteCoordinator) getRemoteRef(this.service.addMember(this.uuid), MembershipManager.COORDINATOR);
  }

  @Override
  public boolean submitJob(JobId jobId) {
    // TODO: implement this functionality to be used from within a Coordinator
    return false;
  }

  @Override
  public boolean leave() {
    return this.service.removeMember(this.uuid);
  }

  /* ---------- RemoteUser methods ---------- */

  @Override
  public Job getJob(JobId jobId) {
    // TODO: implement this functionality to be used from within a JobManager
    return null;
  }

  @Override
  public void setJobResult(JobId jobId, JobResult results) {
    // TODO: implement this functionality to be used from within a JobManager
  }

  /* ---------- Coordinator methods ---------- */



  /* ---------- RemoteCoordinator methods ---------- */

  @Override
  public void addPeer(Uuid peer) {
    synchronized (this.availablePeers) {
      this.availablePeers.put(peer.toString(), peer);
    }
  }

  @Override
  public void removePeer(Uuid peer) {
    synchronized (this.availablePeers) {
      this.availablePeers.remove(peer.toString());
    }
  }

  @Override
  public Uuid getActivePeer() {
    // iterate through available peers, return first "live" peer, remove "dead" peers
    for (String uuid : this.availablePeers.keySet()) {
      try {
        RemoteUser user = (RemoteUser) getRemoteRef(this.availablePeers.get(uuid), MembershipManager.USER);
        return user.getUuid();
      } catch (RemoteException | NotBoundException ex1) {
        try {
          this.service.removeMember(this.availablePeers.get(uuid));
        } catch (RemoteException | NotBoundException ex2) {
          // TODO: handle this exception
        }
      }
    }

    // TODO: handle situation where there are no active peers
    // TODO: beware of peer pinging itself; is this all right?
    return null;
  }

  @Override
  public Map<String, Uuid> getActivePeers() {
    return new HashMap<>(this.availablePeers);
  }

  @Override
  public void setActivePeers(Map<String, Uuid> activePeers) {
    this.availablePeers.clear();
    this.availablePeers.putAll(activePeers);
  }

  @Override
  public boolean assignJob(JobId jobId) {
    // TODO: implement this functionality to be used from within a JobManager
    return false;
  }

  @Override
  public List<RemoteTaskManager> getTaskManagers() {
    // TODO: implement this functionality to be used from within a JobManager
    return null;
  }

  /* ---------- JobManager methods ---------- */



  /* ---------- RemoteJobManager methods ---------- */

  @Override
  public JobResult manageJob(JobId jobId) {
    // TODO: implement this functionality to be used from within a Coordinator
    return null;
  }

  /* ---------- TaskManager methods ---------- */



  /* ---------- RemoteTaskManager methods ---------- */

  @Override
  public TaskResult performTask(Task task) {
    // TODO: implement this functionality to be used from within a JobManager
    return null;
  }

  /* ---------- Communicate methods ---------- */

  @Override
  public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), RemoteMembershipManager.PORT);
    return registry.lookup(uuid.toString() + peerRole);
  }

  /* ---------- Identify methods ---------- */

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}