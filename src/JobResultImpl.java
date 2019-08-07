public class JobResultImpl implements JobResult {
    private Job job;
    private String status;
    private DataSet dataSet;

    public void JobResult(Job job, String status, DataSet dataSet) {
        this.job = job;
        this.status = status;
        this.dataSet = dataSet;
    }

    public Uuid getUserUuid() {
        return job.getUuid();
    }

    public JobId getJobId() {
        return job.getJobId();
    }

    public String getStatus() {
        return this.status;
    }

    public DataSet getResults() {
        return this.dataSet;
    }
}
