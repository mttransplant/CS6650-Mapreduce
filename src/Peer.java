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

public class Peer implements User, RemoteUser, Coordinator, RemoteCoordinator, JobManager, RemoteJobManager, TaskManager, RemoteTaskManager {
  private RemoteMembershipManager service;
  private Uuid uuid;
  private RemoteCoordinator coordinator;
  private final Map<String, Uuid> availablePeers;
  private List<Job> jobs;

  public Peer() {
    this.availablePeers = new HashMap<>();

    try {
      // connect to the MembershipService and get a Uuid
      Registry remoteRegistry = LocateRegistry.getRegistry(MembershipManager.SERVICE_HOST, MembershipManager.PORT);
      this.service = (RemoteMembershipManager) remoteRegistry.lookup(MembershipManager.SERVICE_NAME);
      this.uuid = this.service.generateUuid(InetAddress.getLocalHost());

      // create references to all Remote Peer interfaces
      RemoteUser user = this;
      RemoteJobManager jobManager = this;
      RemoteTaskManager taskManager = this;

      // get a stub for each of these Remote Peer interfaces
      RemoteUser userStub = (RemoteUser) UnicastRemoteObject.exportObject(user, MembershipManager.PORT);
      RemoteJobManager jobManagerStub = (RemoteJobManager) UnicastRemoteObject.exportObject(jobManager, MembershipManager.PORT);
      RemoteTaskManager taskManagerStub = (RemoteTaskManager) UnicastRemoteObject.exportObject(taskManager, MembershipManager.PORT);

      // create a local registry... or simply get it if it already exists
      Registry localRegistry;

      try {
        localRegistry = LocateRegistry.createRegistry(MembershipManager.PORT);
      } catch (RemoteException re) {
        localRegistry = LocateRegistry.getRegistry(MembershipManager.PORT);
      }

      // register this Peer as a RemoteUser, RemoteCoordinator, RemoteJobManager, and RemoteTaskManager
      localRegistry.rebind(getUuid().toString() + MembershipManager.USER, userStub);
      localRegistry.rebind(getUuid().toString() + MembershipManager.JOB_MANAGER, jobManagerStub);
      localRegistry.rebind(getUuid().toString() + MembershipManager.TASK_MANAGER, taskManagerStub);
    } catch (UnknownHostException uhe) {
      // TODO: handle this exception better?
      System.out.println(String.format("UnkownHoustException encountered launching Peer: %s", uhe.getMessage()));
    } catch (RemoteException re) {
      // TODO: then handle this exception better?
      System.out.println(String.format("RemoteException encountered launching Peer: %s", re.getMessage()));
    } catch (NotBoundException nbe) {
      // TODO: then handle this exception better too?
      System.out.println(String.format("NotBoundException encountered launching Peer: %s", nbe.getMessage()));
    }
  }

  /* ---------- User methods ---------- */

  @Override
  public void join() {
    try {
      Uuid coord = this.service.addMember(this.uuid);
      this.coordinator = (RemoteCoordinator) getRemoteRef(coord, MembershipManager.COORDINATOR);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
    }
  }

  @Override
  public void submitJob(JobId jobId) {
    try {
      this.coordinator.assignJob(jobId);
    } catch (RemoteException re) {
      // get a new coordinator from the MembershipManager
      // ping coordinator (as user)... if dead, forcibly remove (perform this "service" on behalf of the network
    }
  }

  @Override
  public void leave() {
    try {
      this.service.removeMember(this.uuid);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: figure out if this exception needs to be caught and, if so, what needs to happen in the catch clause
    }
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

  @Override
  public void bindCoordinator() {
    try {
      RemoteCoordinator coordinatorStub = (RemoteCoordinator) UnicastRemoteObject.exportObject(coordinator, MembershipManager.PORT);
      Registry localRegistry;

      try {
        localRegistry = LocateRegistry.createRegistry(MembershipManager.PORT);
      } catch (RemoteException re) {
        localRegistry = LocateRegistry.getRegistry(MembershipManager.PORT);
      }

      localRegistry.rebind(getUuid().toString() + MembershipManager.COORDINATOR, coordinatorStub);
    } catch (RemoteException ex) {
      // TODO: handle this exception
    }
  }

  @Override
  public void unbindCoordinator() {
    try {
      Registry localRegistry;

      try {
        localRegistry = LocateRegistry.createRegistry(MembershipManager.PORT);
      } catch (RemoteException re) {
        localRegistry = LocateRegistry.getRegistry(MembershipManager.PORT);
      }

      localRegistry.unbind(getUuid().toString() + MembershipManager.COORDINATOR);
    } catch (RemoteException | NotBoundException ex) {
      // TODO: handle this exception
    }
  }

  @Override
  public boolean hasMinimumResources() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() > MembershipManager.MIN_MEMORY && runtime.availableProcessors() > MembershipManager.MIN_PROCESSORS;
  }

  /* ---------- Coordinator methods ---------- */

  @Override
  public void assignJobToJobManager(JobId jobId) {
    // TODO: implement functionality to pick a JobManager and assign the Job
  }


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
  public void assignJob(JobId jobId) {
    // TODO: implement this functionality to be called by a User
  }

  @Override
  public List<RemoteTaskManager> getTaskManagers() {
    // TODO: implement this functionality to be called by a JobManager
    return null;
  }

  /* ---------- JobManager methods ---------- */

  @Override
  public Job retrieveJob(JobId jobId) {
    // TODO: implement this functionality to access getJob(JobId jobid) on the RemoteUser
    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobId.getSubmitter(),MembershipManager.USER);
      return user.getJob(jobId);
    } catch (RemoteException | NotBoundException e) {
      // TODO: handle this exception
    }
    return null;
  }

  @Override
  public void submitTask(Task task) {
    // TODO: implement this functionality to send performTask(Task task) to a TaskManager
  }

  @Override
  public void returnResults(JobResult jobResult) {
    // TODO: implement this functionality to return the JobResult to a RemoteUser
    try {
      RemoteUser user = (RemoteUser) getRemoteRef(jobResult.getUserUuid(), MembershipManager.USER);
      user.setJobResult(jobResult.getJobId(), jobResult);
    } catch (RemoteException | NotBoundException e) {
      // TODO: handle this exception
    }
  }

  @Override
  public List<RemoteTaskManager> requestTaskManagers(int numberOfPeers) {
    // TODO: implement this functionality to get a list of TaskManagers from RemoteCoordinator
    return null;
  }


  /* ---------- RemoteJobManager methods ---------- */

  @Override
  public JobResult manageJob(JobId jobId) {
    // TODO: implement this functionality to be called by a Coordinator
    return null;
  }

  @Override
  public void submitTaskResult(TaskResult taskResult) {
    // TODO: implement this functionality to be used from within a TaskManager
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
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), MembershipManager.PORT);
    return registry.lookup(uuid.toString() + peerRole);
  }

  /* ---------- Identify methods ---------- */

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}