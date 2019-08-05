import java.rmi.Remote;
import java.util.List;

/**
 * an interface to represent a coordinator of communication amongst peers in a peer-to-peer map/reduce service
 *
 * must maintain a list of Uuids for all available (non-Coordinator) Peers
 * enforces load-balancing policy (work distribution based on Peer capacity)
 * its implementation must provide a constructor that sets its list of available Peers using a pre-existing list
 */
interface RemoteCoordinator extends Remote {
  /**
   * a method to add a Peer to this RemoteCoordinator's list of available Peers
   * called by a MembershipManager
   *
   * @param peer the Uuid of the Peer to be added
   * @return true if the addition was successful, false otherwise
   */
  boolean addPeer(Uuid peer);

  /**
   * a method to remove a Peer from this RemoteCoordinator's list of available Peers
   * called by a MembershipManager
   *
   * @param peer the Uuid of the Peer to be removed
   * @return true if the removal was successful, false otherwise
   */
  boolean removePeer(Uuid peer);

  /**
   * a method to designate an available Peer as a Coordinator
   * called by a MembershipManager
   *
   * @return the Uuid of the Peer to be designated as a Coordinator
   */
  Uuid designateCoordinator();

  /**
   * a method to get the list of active Peers from this RemoteCoordinator
   * called by the MembershipManager when bringing new RemoteCoordinators online
   *
   * @return the list of available Uuids from this RemoteCoordinator
   */
  List<Uuid> getActivePeers();

  /**
   * a method to submit a JobId to a RemoteCoordinator
   * called by a User
   * delegates responsibility to its corresponding Coordinator method
   *
   * @param jobId the JobId of the Job being submitted
   * @return true of the job submission was successful, false otherwise
   */
  boolean submitJob(JobId jobId);

  /**
   * a method to get an allocation of TaskManagers
   * called by a JobManager
   *
   * @return a list of RemoteTaskManagers to be used by the calling JobManager
   */
  List<RemoteTaskManager> getTaskManagers();
}