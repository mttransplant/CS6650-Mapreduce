import MapReduce.Mapper;
import MapReduce.Reducer;

import java.io.Serializable;
import java.util.List;

// this class represents all elements of the Job that is submitted by the user
public class JobImpl implements Job, Serializable {
    private Uuid userUuid; // retained to link this Job to the submitter
    private JobId jobId; // contains the header information that is relevant for this job
    private JobData jobData; // contains the package of data that will be processed by this job
    private Mapper mapper; // contains the Mapper function that the submitter specified
    private Reducer reducer; // contains the Reducer function that the submitted specified

    // default initializer
    public JobImpl(Uuid userUuid, JobId jobId, JobData jobData, Mapper mapper, Reducer reducer) {
        this.userUuid = userUuid;
        this.jobId = jobId;
        this.jobData = jobData;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    // method to return the submitter's UserUuid
    public Uuid getUserUuid() {
        return userUuid;
    }

    // method to return the JobId assigned to this Job
    public JobId getJobId() {
        return jobId;
    }

    // method to return the Job's DataSet that was provided by the submitter
    public JobData getDataset() {
        return jobData;
    }

    // method to retrieve and return a list of subsets of the Job's data
    public List<JobData> getSplitData(int splitSize) {
        return jobData.splitData(splitSize);
    }

    // method to return the mapper function that was specified by the submitter
    public Mapper getMapper() {
        return mapper;
    }

    // method to return the reducer function that was specified by the submitter
    public Reducer getReducer() {
        return reducer;
    }
}
