/**
 * This class creates an object that encapsulates all relevant elements for the results of a Job,
 * ready to be returned to the submitter
 */
public class JobResultImpl implements JobResult {
    private Job job;
    private String status;
    private ResultData resultData;

    // default initializer
    public JobResultImpl(Job job, String status, ResultData resultData) {
        this.job = job;
        this.status = status;
        this.resultData = resultData;
    }

    // return the UserUuid of the user that submitted the job
    public Uuid getUserUuid() {
        return this.job.getUserUuid();
    }

    // return the JobId that is assigned to the Job that produced these results
    public JobId getJobId() {
        return this.job.getJobId();
    }

    // return the status of this object's results
    public String getStatus() {
        return this.status;
    }

    // return this object's results
    public ResultData getResults() {
        return this.resultData;
    }

    // a method used to print this object's results to the terminal
    @Override
    public void print() {
        System.out.println(String.format("Results for job %s...\n", this.job.getJobId().getJobIdNumber()));
        this.resultData.print();
        System.out.println();
    }
}