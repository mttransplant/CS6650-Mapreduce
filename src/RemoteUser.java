import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * an interface to enable remote communication between a user of a peer-to-peer map/reduce service
 * and the peer in the network that is managing the coordination/completion of its job submission
 */
public interface RemoteUser extends Remote, Identify {
  /**
   * a method to get the Job associated with the given JobId
   * called by a JobManager
   *
   * @param jobId the JobId of the Job to be returned
   * @return the Job associated with the given JobId
   */
  Job getJob(JobId jobId) throws RemoteException;

  /**
   * a method to set the results for the given jobId
   * called by a JobManager
   *
   * @param results the results associated with the given jobId
   */
  void setJobResult(JobId jobId, JobResult results) throws RemoteException;

  /**
   * a method to set this User as a Coordinator
   * called by the MembershipManager
   *
   * @throws RemoteException
   */
  void setAsCoordinator() throws RemoteException;

  /**
   * a method to deselect this User as a Coordinator
   * called by the MembershipManager

   * @throws RemoteException
   */
  void unbindCoordinator() throws RemoteException;

  /**
   * a method to check if this User has the minimum required computing resources to join the network
   *
   * @return true if it does, false otherwise
   * @throws RemoteException
   */
  boolean hasMinimumResources() throws RemoteException;
}