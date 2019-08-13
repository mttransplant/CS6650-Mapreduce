import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * an interface that facilitates remote communication in a peer-to-peer network
 * by allowing remote references to identify themselves to their remote callers
 */
public interface Identify extends Remote {
  /**
   * a method to return the Uuid of this RemoteCoordinator
   *
   * @return the Uuid of this Coordinator
   */
  Uuid getUuid() throws RemoteException;
}