import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

class Peer implements User, RemoteUser, Coordinator, RemoteCoordinator, JobManager, RemoteJobManager, TaskManager, RemoteTaskManager {
  private RemoteMembershipManager service;
  private Uuid uuid;

  public Peer() {
    // TODO: register its various remote roles on its own registry

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
  public boolean submitJob(JobId jobId) {
    return false;
  }

  @Override
  public boolean leave() {
    return false;
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
  public Uuid designateCoordinator() {
    return null;
  }

  @Override
  public List<Uuid> getActivePeers() {
    return null;
  }

  @Override
  public List<RemoteTaskManager> getTaskManagers() {
    return null;
  }

  @Override
  public JobResult manageJob(JobId jobId) {
    return null;
  }

  @Override
  public TaskResult performTask(Task task) {
    return null;
  }

  @Override
  public Job getJob(JobId jobId) {
    return null;
  }

  @Override
  public void setJobResult(JobId jobId, JobResult results) {

  }

  @Override
  public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(uuid.getAddress().getHostName(), RemoteMembershipManager.PORT);
    return registry.lookup(uuid.toString() + peerRole);
  }

  @Override
  public Uuid getUuid() {
    return this.uuid;
  }
}