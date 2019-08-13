import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/***
 * an interface to represent the manager of group membership for a peer-to-peer map/reduce service
 *
 * must maintain a list of all designated RemoteCoordinators,
 * adding and removing RemoteCoordinators as necessary
 *
 * is assumed to always be available at a published (universally known) IP address
 */
public interface RemoteMembershipManager extends Remote {
  /**
   * a method to generate a Uuid for a Peer
   * called by a new User
   *
   * @param memberAddress InetAddress of the User requesting a Uuid
   * @return a newly generated Uuid for the invoking new User
   */
  Uuid generateUuid(InetAddress memberAddress, int clientPort) throws RemoteException;

  /**
   * a method to register a new User with the network
   * called by a new User
   * designates this new User as a RemoteCoordinator if one is needed
   *
   * @param newMember the Uuid of the new Peer
   * @return a reference to a RemoteCoordinator through which the invoking new User can submit jobs
   */
  Uuid addMember(Uuid newMember) throws RemoteException, NotBoundException;

  /**
   * a method to remove a User from the network
   * called by a User
   *
   * if this User being removed is a RemoteCoordinator,
   * the MembershipManager must remove this User from its list of RemoteCoordinators
   * and then designate a new RemoteCoordinator if required
   *
   * @param uuid the Uuid of the User to be removed
   */
  void removeMember(Uuid uuid)  throws RemoteException, NotBoundException;

  /**
   * a method to get a new Coordinator Uuid
   * called by a User
   *
   * @return the Uuid of a Coordinator
   * @throws RemoteException
   */
  Uuid getNewCoordinator() throws RemoteException;
}