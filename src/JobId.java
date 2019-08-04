import java.util.UUID;

class JobId {
  private Uuid userId;
  private String jobId;

  public JobId(Uuid userId) {
    this.userId = userId;
    this.jobId = UUID.randomUUID().toString();
  }

  Uuid getUserId() { return this.userId; }
  String getJobId() { return this.jobId; }
}