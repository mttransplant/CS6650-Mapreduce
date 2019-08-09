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
  // TODO: determine what methods are needed here

    // called to collect the Job from the User, given a JobId
    Job retrieveJob(JobId jobId);

    // called to submit a task to a TaskManager for execution
    void submitTasks(List<Task> task);

    // called to return the JobResults back to the RemoteUser
    void returnResults(JobResult jobResult);

    // called to get a list of available TaskManagers from the Coordinator
    List<RemoteTaskManager> requestTaskManagers(int numberOfPeers);
}
