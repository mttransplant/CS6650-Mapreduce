import java.rmi.Remote;

/**
 * an interface to enable remote communication between a user of a peer-to-peer map/reduce service
 * and the peer in the network that is managing the coordination/completion of its job submission
 */
interface RemoteUser extends Remote {
  /**
   * a method to get the Job associated with the given JobId
   * called by a JobManager
   *
   * @param jobId the JobId of the Job to be returned
   * @return the Job associated with the given JobId
   */
  Job getJob(JobId jobId);

  /**
   * a method to set the results for the given jobId
   * called by a JobManager
   *
   * @param results the results associated with the given jobId
   */
  void setJobResult(JobId jobId, JobResult results); // called from the JobManager
}