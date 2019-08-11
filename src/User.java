import java.util.Map;

import MapReduce.Mapper;
import MapReduce.Reducer;

/**
 * an interface to represent a user of a peer-to-peer map/reduce service
 *
 * must maintain a reference to the RemoteMembershipManager
 * must maintain a references to a RemoteCoordinator
 *
 * if it encounters a dead RemoteCoordinator when attempting to submit a job,
 * it must leave() and then join() again before retrying job submission
 */
public interface User extends Communicate {
  /**
   * a method that allows this User to join the service
   * delegates responsibility to the RemoteMembershipManager
   */
  void join();

  /**
   * a method that creates a job from the given id, data, mapper, and reducer
   *
   * @param id the id for this job
   * @param data the data for this job
   * @param mapper the mapper to be used for this job
   * @param reducer the reducer to be used for this job
   */
  void createJob(JobId id, JobData data, Mapper mapper, Reducer reducer);

  /**
   * a method that allows this User to initiate the process of submitting a Job
   * delegates responsibility to its RemoteCoordinator
   *
   * @param jobId a universally unique identifier for the job to be submitted
   */
  void submitJob(JobId jobId);

  /**
   * a method that returns the map of this user's created jobs
   *
   * @return a map of this user's created jobs
   */
  Map<String, Job> getJobs();

  /**
   * a mehtod that returns the map of this user's job results
   *
   * @return a map of this user's job results
   */
  Map<String, JobResult> getResults();

  /**
   * a method that allows this User to leave the sevice
   * delegates responsibility to the RemoteMembershipManager
   */
  void leave();
}