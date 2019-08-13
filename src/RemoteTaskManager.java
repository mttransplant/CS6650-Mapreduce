import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
public interface RemoteTaskManager extends Remote, Identify {
  TaskResult performMapTask(Task task, List<Uuid> reducerIds) throws RemoteException; // called from a JobManager
  void submitMapResult(String key, int value, JobId jobId) throws RemoteException;
  TaskResult performReduceTask(Task task) throws RemoteException;
//  void setReducerIds(List<Uuid> uuids) throws RemoteException;
}