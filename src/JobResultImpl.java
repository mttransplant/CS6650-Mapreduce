import java.io.Serializable;

public class JobResultImpl implements JobResult {
    private Job job;
    private String status;
    private ResultData resultData;

    public void JobResult(Job job, String status, ResultData resultData) {
        this.job = job;
        this.status = status;
        this.resultData = resultData;
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

    public ResultData getResults() {
        return this.resultData;
    }
}
