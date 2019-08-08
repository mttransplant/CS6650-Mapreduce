import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
public interface RemoteTaskManager extends Remote, Identify {
  TaskResult performTask(Task task) throws RemoteException; // called from a JobManager
}