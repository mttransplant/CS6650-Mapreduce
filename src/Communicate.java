import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * an interface that allows Peers/Service to communicate remotely with Peers
 * given only the Uuid of that Peer
 */
public interface Communicate {
  /**
   * a method to get a remote reference to a Peer given its Uuid
   *
   * @param uuid the Uuid of the Peer to be communicated with
   * @param peerRole the role of the Peer to be communicated with (e.g., USER, COORDINATOR, JOB_MANAGER, TASK_MANAGER)
   *
   * @return a Remote reference to that Peer (that must be type-cast into a specific role)
   * @throws RemoteException
   * @throws NotBoundException
   */
  Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException;
}