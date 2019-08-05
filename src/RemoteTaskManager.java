import java.rmi.Remote;

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
interface RemoteTaskManager extends Remote, Identify {
  TaskResult performTask(Task task); // called from a JobManager
}