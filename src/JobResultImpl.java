public class JobResultImpl implements JobResult {
    private Job job;
    private String status;
    private JobData jobData;

    public void JobResult(Job job, String status, JobData jobData) {
        this.job = job;
        this.status = status;
        this.jobData = jobData;
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

    public JobData getResults() {
        return this.jobData;
    }
}
