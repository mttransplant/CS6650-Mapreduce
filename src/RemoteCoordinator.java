import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * an interface to represent a coordinator of communication amongst peers in a peer-to-peer map/reduce service
 *
 * must maintain a list of Uuids for all available (non-Coordinator) Peers
 * enforces load-balancing policy (work distribution based on Peer capacity)
 * its implementation must provide a constructor that sets its list of available Peers using a pre-existing list
 */
interface RemoteCoordinator extends Remote, Identify {
  /**
   * a method to add a Peer to this RemoteCoordinator's list of available Peers
   * called by a MembershipManager
   *
   * @param peer the Uuid of the Peer to be added
   */
  void addPeer(Uuid peer) throws RemoteException;

  /**
   * a method to remove a Peer from this RemoteCoordinator's list of available Peers
   * called by a MembershipManager
   *
   * @param peer the Uuid of the Peer to be removed
   */
  void removePeer(Uuid peer) throws RemoteException;

  /**
   * a method to get the Uuid of a live Peer in this Coordinator's list of active Peers
   * remove any dead Peers encountered along the way from the service
   *
   * @return the Uuid of a live Peer
   */
  Uuid getActivePeer() throws RemoteException;

  /**
   * a method to get the map of active Peers from this RemoteCoordinator
   * called by the MembershipManager when bringing new RemoteCoordinators online
   *
   * @return the map of available Uuids from this RemoteCoordinator
   */
  Map<String, Uuid> getActivePeers() throws RemoteException;

  /**
   * a method to set the map of active Peers for this RemoteCoordinator
   * called by the MembershipManager when bringing new RemoteCoordinators online
   */
  void setActivePeers(Map<String, Uuid> activePeers) throws RemoteException;

  /**
   * a method to assign a JobId to a JobCoordinator
   * called by a User
   * delegates responsibility to its corresponding Coordinator method
   *
   * @param jobId the JobId of the Job being assigned
   */
  void assignJob(JobId jobId) throws RemoteException;

  /**
   * a method to get an allocation of TaskManagers
   * called by a JobManager
   *
   * @return a list of RemoteTaskManagers to be used by the calling JobManager
   */
  List<RemoteTaskManager> getTaskManagers() throws RemoteException;
}