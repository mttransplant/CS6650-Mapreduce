import java.util.UUID;

public class TaskId {
    private Uuid submitter;
    private String jobId;
    private String taskId;

    public TaskId(Uuid userId, String jobId) {
        this.submitter = userId;
        this.jobId = jobId;
        this.taskId = UUID.randomUUID().toString();
    }

    Uuid getSubmitter() { return this.submitter; }
    String getJobId() { return this.jobId; }
    String getTaskId() { return this.taskId; }

}


