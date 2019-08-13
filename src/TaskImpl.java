import MapReduce.Mapper;
import MapReduce.Reducer;
import java.io.Serializable;

// an object to represent the Task that will be passed from a JobManager to a TaskManager
public class TaskImpl implements Task, Serializable {
    private TaskId taskId; // the "header" information for this Task
    private Uuid userUuid; // the Uuid of the submitter associated with this Task
    private Uuid jobManagerUuid; // the Uuid of the JobManager that assigned this Task
    private JobData jobData; // the data segment that has been assigned to this Task
    private Mapper mapper; // the Mapper function that will be used by this Task
    private Reducer reducer; // the Reducer function that will be used by this Task

    // default initializer
    public TaskImpl(TaskId taskId, Uuid userUuid, Uuid jobManagerUuid, JobData jobData, Mapper mapper, Reducer reducer) {
        this.taskId = taskId;
        this.userUuid = userUuid;
        this.jobManagerUuid = jobManagerUuid;
        this.jobData = jobData;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    // method to return this Task's TaskId
    public TaskId getTaskId() {
        return taskId;
    }

    // method to return this Task's submitter Uuid
    public Uuid getPeerUuid() {
        return userUuid;
    }

    // method to return this Task's JobManager Uuid
    public Uuid getJobManagerUuid() {
        return jobManagerUuid;
    }

    // method to return this Task's data
    public JobData getDataset() {
        return jobData;
    }

    // method to return this Task's Mapper function
    public Mapper getMapper() {
        return mapper;
    }

    // method to return this Task's Reducer function
    public Reducer getReducer() {
        return reducer;
    }
}
