/**
 * an interface to represent a user of a peer-to-peer map/reduce service
 *
 * must maintain a reference to the RemoteMembershipManager
 * must maintain a references to a RemoteCoordinator
 *
 * if it encounters a dead RemoteCoordinator when attempting to submit a job,
 * it must leave() and then join() again before retrying job submission
 */
interface User extends RemoteUser {
  /**
   * a method that allows this User to join the service
   * delegates responsibility to the RemoteMembershipManager
   *
   * @return a reference to a RemoteCoordinator
   */
  RemoteCoordinator join();

  /**
   * a method that allows this User to initiate the process of submitting a Job
   * delegates responsibility to its RemoteCoordinator
   *
   * @param jobId a universally unique identifier for the job to be submitted
   * @return true if the job submission process was successfully initiated, false otherwise
   */
  boolean submitJob(JobId jobId);

  /**
   * a method that allows this User to leave the sevice
   * delegates responsibility to the RemoteMembershipManager
   *
   * @return true if the User successfully left the service, false otherwise
   */
  boolean leave();
}