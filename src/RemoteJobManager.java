import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * must maintain separate lists of references to all RemoteCoordinators and each of its backup RemoteJobManagers
 * all "replica" JobManagers for a Job should proceed randomly through the job and report interim results back to its fellows (for redundancy)
 */
public interface RemoteJobManager extends Remote, Identify {
  // called from a Coordinator, initiates task execution, delegates back
  // to RemoteCoordinator for RemoteTaskManager allocation
  void manageJob(JobId jobId) throws RemoteException, NotBoundException;
}