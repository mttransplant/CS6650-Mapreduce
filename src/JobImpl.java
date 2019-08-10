import MapReduce.Mapper;
import MapReduce.Reducer;

import java.io.Serializable;
import java.util.List;

public class JobImpl implements Job, Serializable {
    private Uuid userUuid;
    private JobId jobId;
    private JobData jobData;
    private Mapper mapper;
    private Reducer reducer;

    public void Job(Uuid userUuid, JobId jobId, JobData jobData, Mapper mapper, Reducer reducer) {
        this.userUuid = userUuid;
        this.jobId = jobId;
        this.jobData = jobData;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    // TODO: Add method and interface to split the passed data into an array of data

    public Uuid getUserUuid() {
        return userUuid;
    }

    public JobId getJobId() {
        return jobId;
    }

    public JobData getDataset() {
        return jobData;
    }

    public List<JobData> getSplitData(int splitSize) {
        return jobData.splitData(splitSize);
    }

    public Mapper getMapper() {
        return mapper;
    }

    public Reducer getReducer() {
        return reducer;
    }
}
