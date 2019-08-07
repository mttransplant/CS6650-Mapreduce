import javax.xml.crypto.Data;
import java.util.ArrayList;

public class ListOfString implements Data {
    private DataSet dataSet = (DataSet) new ArrayList<String>();
    // TODO: Ask the team: Is this structure okay, or should be something else?


    DataSet getDataset() {
        return dataSet;
    }
    void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
}
