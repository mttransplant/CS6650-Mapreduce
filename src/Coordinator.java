public interface Coordinator extends Communicate {
  // TODO: determine what methods are needed here

    /**
     * a method to select an available JobManager and assign it to the requested job
     *
     * @param jobId contains the User Uuid and job number
     */
    void assignJobToJobManager(JobId jobId);
}
