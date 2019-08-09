import MapReduce.Mapper;
import MapReduce.Reducer;

import java.util.List;

public class JobImpl implements Job {
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

    public Uuid getUuid() {
        return userUuid;
    }

    public JobId getJobId() {
        return jobId;
    }

    public JobData getDataset() {
        return jobData;
    }

    public List<List<String>> getSplitData(int splitSize) {
        return jobData.splitData(splitSize);
    }

    public Mapper getMapper() {
        return mapper;
    }

    public Reducer getReducer() {
        return reducer;
    }
}
