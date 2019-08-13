import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * does the Task its assigned and reports back its IntermediateResult
 */
public interface RemoteTaskManager extends Remote, Identify {
  /**
   * called by the JobManager
   * @param task Object containing all the details of the Task
   * @param reducerIds list of all the reducerIds that have been requested for the reduce portion of the Map/Reduce process
   * @return TaskResult, an intermediary result package
   * @throws RemoteException in case performMapTask cannot reach the Reducers
   */
  TaskResult performMapTask(Task task, List<Uuid> reducerIds) throws RemoteException;

  /**
   * called by the TaskManager to submit the intermediary results to the appropriate reducer.
   * @param key identifying the segment of the map results that is being submitted
   * @param value identifying the value that will be sent to the reducer
   * @param jobId identifying the Job that these results belong to
   * @throws RemoteException in case submitMapResult cannot reach the Reducer
   */
  void submitMapResult(String key, int value, JobId jobId) throws RemoteException;
  TaskResult performReduceTask(Task task) throws RemoteException;
}