import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public class ReduceTask implements TaskManager {
    private String key;
    private List<Integer> values;

    @Override
    public Remote getRemoteRef(Uuid uuid, String peerRole) throws RemoteException, NotBoundException {
        return null;
    }

    public void buffer(int value) {
        values.add(value);
    }
}
