/**
 * an interface for methods needed to act as a Coordinator in a peer-to-peer Map/Reduce network
 */
public interface Coordinator extends Communicate {
    /**
     * a method to select an available JobManager and assign it to the requested job
     *
     * @param jobId contains the User Uuid and job number
     */
    void assignJobToJobManager(JobId jobId);
}