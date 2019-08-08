import java.util.ArrayList;
import java.util.List;

public class JobData {
    private List<String> jobData;

    // this initializer is used by the User to provide raw data
    JobData(List<String> dataSet) {
        this.jobData = dataSet;
    }

    public List<String> getJobData() {
        return jobData;
    }

    public List<List<String>> splitData(int splitSize) {
        List<List<String>> splitData = new ArrayList<>();
        int dataSetSize = jobData.size();
        for (int i = 0; i < dataSetSize; i += splitSize) {
            splitData.add(new ArrayList<>(
                    jobData.subList(i, Math.min(dataSetSize, i + splitSize))
            ));
        }
        return splitData;
    }
}
