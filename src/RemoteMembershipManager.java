import java.net.InetAddress;
import java.rmi.Remote;

/*
 * 1. establish MembershipManager for service
 * 2. new User reaches out to MembershipManager to join service (1st and then 1 out of every x Peers are assigned as Coordinators)
 * 3. MembershipManager informs new User a live Coordinator
 * 4. User submits Job to Coordinator
 * 5. Coordinator selects JobManagers (Coordinator can't be JobManager or TaskManager)
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
 * must maintain a list of Uuids for all designated RemoteCoordinators,
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
   * @param newMember the Uuid of the new Peer
   * @return a reference to a RemoteCoordinator through which the invoking new User can submit jobs
   */
  Uuid addMember(Uuid newMember);

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