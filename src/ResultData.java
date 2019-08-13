import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ResultData implements Serializable {
    private Map<String, Integer> resultData;

    // this initializer is used by the TaskManager and JobManager
    // to return the outcome of the Map and Reduce functions
    ResultData(Map<String, Integer> dataSet) {
        this.resultData =  dataSet;
    }

    public Map<String, Integer> getResultData() {
        return this.resultData;
    }

    public void print() {
        System.out.println("word : number of times word appeared in document");
        for (String word : this.resultData.keySet()) {
            System.out.println(String.format("%s : %d", word, this.resultData.get(word)));
        }
    }
}
