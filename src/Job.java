// should have the Uuid of the submitting User as a field

import MapReduce.Mapper;
import MapReduce.Reducer;
import java.io.Serializable;
import java.util.List;


/**
 * an interface to represent the job that will be passed from a peer to a JobManager
 *
 * must capture a Uuid, a dataset, a MapReduce.Mapper, and a MapReduce.Reducer
 */

public interface Job extends Serializable {

    /**
     * a method to retrieve the Uuid of the peer that submitted the job
     * called by the JobManager assigned to the job so that it can be
     * packaged into a Task that is sent to a TaskManager
     *
     * @return the Uuid of the originating peer
     */
    Uuid getUuid();

    /**
     * a method to retrieve the JobId of the job.
     * called by JobManager when returning the JobResults
     * @return JobId of the completed Job
     */
    JobId getJobId();

    /**
     * a method to retrieve the dataset object that the user provided, which will
     * be processed by the passed MapReduce.Mapper and MapReduce.Reducer functions
     * called by the JobManager assigned to the job
     *
     * @return the File of data to be processed
     */
    JobData getDataset();

    public List<List<String>> getSplitData(int splitSize);

    /**
     * a method to retrieve the MapReduce.Mapper function object that the user provided
     * called by the JobManager so it can repackage the MapReduce.Mapper in the Task
     * The MapReduce.Mapper is a user defined object that contains the desired mapping
     * functionality
     *
     * @return the MapReduce.Mapper function object
     */
    Mapper getMapper();
    // TODO: Connect with Nay on naming and implementation details

    /**
     * a method to retrieve the MapReduce.Reducer function object that the user provided
     * called by the JobManager so it can repackage the MapReduce.Reducer in the Task
     * may also be called by the JobManager to reduce the sets returned by each
     * TaskManager
     *
     * The MapReduce.Reducer is a user defined object that contains the desired reduction
     * functionality
     *
     * @return the MapReduce.Mapper function object
     */
    Reducer getReducer();
    // TODO: Connect with nay on naming and implementation details
}
