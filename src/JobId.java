import java.io.Serializable;
import java.util.UUID;

public class JobId implements Serializable {
  private Uuid submitter;
  private String jobIdNumber;
  private long jobSize;

  public JobId(Uuid userId, long jobSize) {
    this.submitter = userId;
    this.jobIdNumber = UUID.randomUUID().toString();
    this.jobSize = jobSize;
  }

  Uuid getSubmitter() { return this.submitter; }
  String getJobIdNumber() { return this.jobIdNumber; }
  long getJobSize() { return this.jobSize; }
}