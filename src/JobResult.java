import java.io.Serializable;

/**
 * an interface to represent the results that will be passed from a JobManager back to
 * the peer that submitted the original Job
 *
 * must capture a Uuid, a dataset, a MapReduce.Mapper, and a MapReduce.Reducer
 */

public interface JobResult extends Serializable {
    String SUCCESS = "success";
    String ERROR = "error";
    /**
     * a method to retrieve the user's Uuid so the results
     * can be returned
     * @return Uuid of the submitting user
     */
    Uuid getUserUuid();

    /**
     * a method to retrieve the JobId of the originating job
     * that produced the current JobResults
     * called by JobManager when communicating results back to User
     * @return JobId created for the job
     */
    JobId getJobId();

    /**
     * a method to retrieve the final status of the job. this is used to inform
     * the peer if the process was successful, or if not, some detail about the
     * errors encountered.
     * Option 1: "SUCCESSFUL"
     * Option 2: "UNSUCCESSFUL: " + error details
     * called by the Peer
     *
     * @return a String
     */
    String getStatus();

    /**
     * a method to retrieve the results object that the JobManager returned
     * called by the Peer
     *
     * @return the File of the processed results
     * TODO: need to decide if file will be null if there were errors
     */
    ResultData getResults();

    /**
     * a method to print these results
     */
    void print();
}
