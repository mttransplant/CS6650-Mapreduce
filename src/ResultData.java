import java.io.Serializable;
import java.util.List;
import java.util.Map;

// this object encapsulates the results of the Map and Reduce processing in the TaskManager
public class ResultData implements Serializable {
    private Map<String, Integer> resultData; // the raw Task results from the TaskManager

    // this initializer is used by the TaskManager and JobManager
    // to return the outcome of the Map and Reduce functions
    ResultData(Map<String, Integer> dataSet) {
        this.resultData =  dataSet;
    }

    // method to return this object's data
    public Map<String, Integer> getResultData() {
        return this.resultData;
    }

    // a method used to print this object's results to the terminal
    public void print() {
        System.out.println("word : number of times word appeared in document");
        for (String word : this.resultData.keySet()) {
            System.out.println(String.format("%s : %d", word, this.resultData.get(word)));
        }
    }
}
