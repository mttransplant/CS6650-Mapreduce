import java.util.Map;

public class ReduceTaskResult implements TaskResult {
    private ResultData resultData;

    ReduceTaskResult(Map<String, Integer> map) {
        resultData = new ResultData(map);
    }

    @Override
    public TaskId getTaskId() {
        return null;
    }

    @Override
    public Uuid getPeerUuid() {
        return null;
    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public ResultData getResults() {
        return resultData;
    }
}
