import java.util.UUID;

class JobId {
  private Uuid submitter;
  private String jobId;
  private long jobSize;

  public JobId(Uuid userId, long jobSize) {
    this.submitter = userId;
    this.jobId = UUID.randomUUID().toString();
    this.jobSize = jobSize;
  }

  Uuid getSubmitter() { return this.submitter; }
  String getJobId() { return this.jobId; }
  long getJobSize() { return this.jobSize; }
}