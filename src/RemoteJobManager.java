import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * must maintain separate lists of references to all RemoteCoordinators and each of its backup RemoteJobManagers
 * all "replica" JobManagers for a Job should proceed randomly through the job and report interim results back to its fellows (for redundancy)
 */
public interface RemoteJobManager extends Remote, Identify {
  // called from a Coordinator, initiates task execution, delegates back
  // to RemoteCoordinator for RemoteTaskManager allocation
  // TODO: Clarify why a Coordinator wants to get the JobResult?
  void manageJob(JobId jobId) throws RemoteException;

  // TODO: delete if it's really not being used, which we think it's not... since the executor and completion service handle this
  // called from a TaskManager, initiates the return of the TaskManager's processing
  void submitTaskResult(TaskResult taskResult) throws RemoteException;
}