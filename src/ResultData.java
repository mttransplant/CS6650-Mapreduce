import java.util.List;
import java.util.Map;

public class ResultData {
    private Map<String, Integer> resultData;

    // this initializer is used by the TaskManager and JobManager
    // to return the outcome of the Map and Reduce functions
    ResultData(Map<String, Integer> dataSet) {
        this.resultData =  dataSet;
    }

    public Map<String, Integer> getResultData() {
        return resultData;
    }

    // splitResultData not needed. The results from the TaskManagers will only be merged.
}
