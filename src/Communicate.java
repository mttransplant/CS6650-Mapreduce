import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Communicate {
  Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException;
}