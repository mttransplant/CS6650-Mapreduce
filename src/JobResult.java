import java.io.File;

/**
 * an interface to represent the results that will be passed from a JobManager back to
 * the peer that submitted the original Job
 *
 * must capture a Uuid, a dataset, a MapReduce.Mapper, and a MapReduce.Reducer
 */

public interface JobResult {

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
    String status();

    /**
     * a method to retrieve the results object that the JobManager returned
     * called by the Peer
     *
     * @return the File of the processed results
     * TODO: need to decide if file will be null if there were errors
     */
    File getResults();
}
