import java.io.Serializable;
import java.util.UUID;

public class TaskId implements Serializable {
    private Uuid submitter;
    private JobId jobId;
    private String taskId;

    public TaskId(Uuid userId, JobId jobId) {
        this.submitter = userId;
        this.jobId = jobId;
        this.taskId = UUID.randomUUID().toString();
    }

    Uuid getSubmitter() { return this.submitter; }
    JobId getJobId() { return this.jobId; }
    String getTaskId() { return this.taskId; }

}


