import java.io.File;

public class JobImpl implements Job {
    private Uuid userUuid;
    private JobId jobId;
    private DataSet dataSet;
    private Mapper mapper;
    private Reducer reducer;

    public void Job(Uuid userUuid, JobId jobId, DataSet dataSet, Mapper mapper, Reducer reducer) {
        this.userUuid = userUuid;
        this.jobId = jobId;
        this.dataSet = dataSet;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    public Uuid getUuid() {
        return userUuid;
    }

    public JobId getJobId() {
        return jobId;
    }

    public DataSet getDataset() {
        return dataSet;
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
