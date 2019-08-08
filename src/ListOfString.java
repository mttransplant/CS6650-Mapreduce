import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Map;

public class ListOfString implements DataSet {
    private DataSet dataSet;
    // TODO: Ask the team: Is this structure okay, or should be something else?

    // this initializer is used by the User to provide raw data
    public void DataSet(ArrayList<String> dataSet) {
        this.dataSet = (DataSet) dataSet;
    }

    // this initializer is used by the TaskManager and JobManager
    // to return the outcome of the Map and Reduce functions
    public void DataSet(Map<String, Integer> dataSet) {
        this.dataSet = (DataSet) dataSet;
    }
    public DataSet getDataset() {
        return dataSet;
    }
}
