import java.util.UUID;

class JobId {
  private Uuid submitter;
  private String jobId;

  public JobId(Uuid userId) {
    this.submitter = userId;
    this.jobId = UUID.randomUUID().toString();
  }

  Uuid getSubmitter() { return this.submitter; }
  String getJobId() { return this.jobId; }
}