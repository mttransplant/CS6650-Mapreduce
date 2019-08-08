import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class MapTask implements TaskManager {
    @Override
    public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
        return null;
    }

}
