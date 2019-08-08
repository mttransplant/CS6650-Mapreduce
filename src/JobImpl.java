import MapReduce.Mapper;
import MapReduce.Reducer;

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

    public Mapper getMapper() {
        // TODO: Connect with Nay on naming and implementation details
        return mapper;
    }

    public Reducer getReducer() {
        // TODO: Connect with nay on naming and implementation details
        return reducer;
    }
}
