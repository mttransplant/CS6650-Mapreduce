import java.io.File;
import java.io.Serializable;

/**
 * an interface to represent the results that will be passed from a TaskManager back to
 * the JobManager that submitted a Task
 *
 */

public interface TaskResult extends Serializable {


    /**
     * a method to retrieve the TaskId.
     * called by the JobManager to track which tasks have completed and which
     * are still outstanding
     * @return the TaskId of the assigned Task
     */
    TaskId getTaskId();

    /**
     * a method to retrieve the Uuid of the peer that submitted the job
     * called by the JobManager assigned to the Job so that the results can be
     * delivered back to the Peer
     *
     * @return the Uuid of the originating peer
     */
    Uuid getPeerUuid();

    /**
     * a method to retrieve the final status of the Task. this is used to inform
     * the JobManager if the process was successful, or if not, some detail about the
     * errors encountered.
     * Option 1: "SUCCESSFUL"
     * Option 2: "UNSUCCESSFUL: " + error details
     * called by the JobManager
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
