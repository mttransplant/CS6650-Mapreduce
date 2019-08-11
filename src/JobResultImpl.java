public class JobResultImpl implements JobResult {
    private Job job;
    private String status;
    private ResultData resultData;

    public JobResultImpl(Job job, String status, ResultData resultData) {
        this.job = job;
        this.status = status;
        this.resultData = resultData;
    }

    public Uuid getUserUuid() {
        return job.getUserUuid();
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

    @Override
    public void print() {
        System.out.println(String.format("Results for job %s...\n", job.getJobId().getJobIdNumber()));
        resultData.print();
        System.out.println();
    }
}