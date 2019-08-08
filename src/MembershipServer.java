import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MembershipServer {
  public static void main(String[] args) {
    try {
      RemoteMembershipManager manager = new MembershipManager();
      RemoteMembershipManager managerStub = (RemoteMembershipManager) UnicastRemoteObject.exportObject(manager, MembershipManager.MANAGER_PORT);
      Registry registry;

      try {
        registry = LocateRegistry.createRegistry(MembershipManager.MANAGER_PORT);
      } catch (RemoteException re) {
        registry = LocateRegistry.getRegistry(MembershipManager.MANAGER_PORT);
      }

      registry.rebind(MembershipManager.SERVICE_NAME, managerStub);

    } catch (RemoteException re) {
      System.out.println("Exception encountered launching MembershipServer");
    }
  }
}