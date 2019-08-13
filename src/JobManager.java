import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * an interface to represent a JobManager of a peer-to-peer map/reduce service
 *
 * must maintain references to all RemoteCoordinators
 * must maintain references to all RemoteJobManagers that are backups for an assigned Job
 * must maintain reference to the RemoteJobManager that is the primary for an assigned Job
 *
 * if it encounters a dead RemoteCoordinator, RemoteJobManager, RemoteTaskManager, or RemoteUser,
 * report that to the MembershipManager
 */
public interface JobManager extends Communicate {

    /**
     * Called by the JobManager to reach out to the user that has requested a JobId. This method
     * will reach out to get the full content of the Job
     * @param jobId contains the header info for the requested Job, so the JobManager can look up the peer.
     * @return the Job object that contains all the details of the job
     * @throws RemoteException
     * @throws NotBoundException
     */
    Job retrieveJob(JobId jobId) throws RemoteException, NotBoundException;

    /**
     * Called to get a list of available TaskManagers from the Coordinator
     * @param num Number ofTaskMangers to request
     * @return List of RemoteTaskManagers
     */
    List<RemoteTaskManager> requestTaskManagers(int num);

    /**
     * called to submit a task to a TaskManger for execution
     * @param tasks is the list of all Tasks that need to be assigned to TaskManagers
     * @return a list of TaskResults that contains the completed output of the Map/Reduce
     */
    List<TaskResult> submitTasks(List<Task> tasks);

    /**
     * called to return the JobResult to the submitting user
     * @param jobResult contains the complete results that need to be returned
     */
    void returnResults(JobResult jobResult);
}
