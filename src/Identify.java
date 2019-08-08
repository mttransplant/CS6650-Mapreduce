import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Identify extends Remote {
  /**
   * a method to return the Uuid of this RemoteCoordinator
   *
   * @return the Uuid of this Coordinator
   */
  Uuid getUuid() throws RemoteException;
}