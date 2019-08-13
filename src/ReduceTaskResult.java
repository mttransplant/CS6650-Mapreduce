import java.util.Map;

/**
 * This class encapsulates the intermediate results from the Reduce function
 */
public class ReduceTaskResult implements TaskResult {
    private ResultData resultData;

    // default initializer
    ReduceTaskResult(Map<String, Integer> map) {
        resultData = new ResultData(map);
    }

    // method to return the TaskId
    @Override
    public TaskId getTaskId() {
        return null;
    }

    // method to return the submitter's Uuid
    @Override
    public Uuid getPeerUuid() {
        return null;
    }

    // method to return the status of these results
    @Override
    public String getStatus() {
        return null;
    }

    // method to return the results encapsulated in this object
    @Override
    public ResultData getResults() {
        return resultData;
    }
}
