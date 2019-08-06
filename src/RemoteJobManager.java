import java.rmi.Remote;

/**
 * must maintain separate lists of references to all RemoteCoordinators and each of its backup RemoteJobManagers
 * all "replica" JobManagers for a Job should proceed randomly through the job and report interim results back to its fellows (for redundancy)
 */
interface RemoteJobManager extends Remote, Identify {
  // called from a Coordinator, initiates task execution, delegates back
  // to RemoteCoordinator for RemoteTaskManager allocation
  // TODO: Clarify why a Coordinator wants to get the JobResult?
  JobResult manageJob(JobId jobId);

  // called from a TaskManager, initiates the return of the TaskManager's processing
  void submitTaskResult(TaskResult taskResult);
}