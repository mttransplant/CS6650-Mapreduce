import MapReduce.Mapper;
import MapReduce.Reducer;

import java.io.File;

public class TaskImpl implements Task {
    private TaskId taskId;
    private Uuid userUuid;
    private Uuid jobManagerUuid;
    private File dataSet;
    private Mapper mapper;
    private Reducer reducer;

    public void Task(TaskId taskId, Uuid userUuid, Uuid jobManagerUuid, File dataSet, Mapper mapper, Reducer reducer) {
        this.taskId = taskId;
        this.userUuid = userUuid;
        this.jobManagerUuid = jobManagerUuid;
        this.dataSet = dataSet;
        this.mapper = mapper;
        this.reducer = reducer;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public Uuid getPeerUuid() {
        return userUuid;
    }

    public Uuid getJobManagerUuid() {
        return jobManagerUuid;
    }

    public File getDataset() {
        return dataSet;
    }

    public Mapper getMapper() {
        // TODO: Connect with Nay on naming and implementation details
        return mapper;
    }

    public Reducer getReducer() {
        // TODO: Connect with nay on naming and implementation details
        return reducer;
    }

}
