import java.io.Serializable;
import java.util.UUID;

// this class is used to maintain the "header" details of the Job.
public class JobId implements Serializable {
  private Uuid submitter; // recorded so the JobManager can return the results to the submitter
  private String jobIdNumber; // a unique identifier assigned to a specific Job
  private long jobSize; // a value used by the JobManager to determine the number of TaskManagers that will be required

  // default initializer
  public JobId(Uuid userId, long jobSize) {
    this.submitter = userId;
    this.jobIdNumber = UUID.randomUUID().toString();
    this.jobSize = jobSize;
  }

  // method to return the Uuid of the submitter
  Uuid getSubmitter() { return this.submitter; }

  // method to return the JobId number of this JobId
  String getJobIdNumber() { return this.jobIdNumber; }

  // method to return the jobSize associated with this JobId
  long getJobSize() { return this.jobSize; }
}