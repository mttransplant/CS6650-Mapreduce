import MapReduce.Mapper;
import MapReduce.Reducer;

import java.io.File;

/**
 * an interface to represent the Task that will be passed from a JobManager to a TaskManager
 *
 */

/* DAN!!!!!! */

public interface Task {

    /**
     * a method to retrieve the TaskId.
     * called by the TaskManager to repackage it in the TaskResult
     * @return the TaskId of the assigned Task
     */
    TaskId getTaskId();

    /**
     * a method to retrieve the Uuid of the peer that submitted the job
     * called by the TaskManager assigned to the Task so that it can be
     * packaged into a TaskResult that is sent back to the JobManager
     *
     * @return the Uuid of the originating peer
     */
    Uuid getPeerUuid();

    /**
     * a method to retrieve the Uuid of the JobManager that submitted the task
     * called by the TaskManager assigned to the Task so that it can return the
     * Map/Reduce results back to the JobManager
     *
     * @return the Uuid of the assigning JobManager
     */
    Uuid getJobManagerUuid();

    /**
     * a method to retrieve the dataset object that the JobManager provided, which
     * is a subset of the dataset that the Peer passed. Will
     * be processed by the passed MapReduce.Mapper and MapReduce.Reducer functions
     * called by the TaskManager assigned to the Task
     *
     * @return the File of data to be processed
     */
    File getDataset();

    /**
     * a method to retrieve the MapReduce.Mapper function object that the user provided
     * called by the TaskManager assigned to a subset of the Job
     * The MapReduce.Mapper is a user defined object that contains the desired mapping
     * functionality
     *
     * @return the MapReduce.Mapper function object
     */
    Mapper getMapper();
    // TODO: Connect with Nay on naming and implementation details

    /**
     * a method to retrieve the MapReduce.Reducer function object that the user provided
     * called by the TaskManager assigned to a subset of the Job
     *
     * The MapReduce.Reducer is a user defined object that contains the desired reduction
     * functionality
     *
     * @return the MapReduce.Mapper function object
     */
    Reducer getReducer();
    // TODO: Connect with nay on naming and implementation details
}
