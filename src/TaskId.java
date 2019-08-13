import java.io.Serializable;
import java.util.UUID;

/**
 * this class contains the "header" information required to track a Task
 */
public class TaskId implements Serializable {
    private Uuid submitter; // retained to track this Task back to the submitting user
    private JobId jobId; // retained to track this Task back to the associated Job
    private String taskId; // this holds the unique ID assigned to this task

    // default initializer
    public TaskId(Uuid userId, JobId jobId) {
        this.submitter = userId;
        this.jobId = jobId;
        this.taskId = UUID.randomUUID().toString();
    }

    // method to retrieve the Uuid of the submitter
    Uuid getSubmitter() { return this.submitter; }

    // method to retrieve the JobId associated with this Task
    JobId getJobId() { return this.jobId; }

    // method to retrieve this Task's TaskId
    String getTaskId() { return this.taskId; }

}


