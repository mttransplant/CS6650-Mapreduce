import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobData implements Serializable {
    private List<String> jobData;

    // this initializer is used by the User to provide raw data
    JobData(List<String> dataSet) {
        this.jobData = dataSet;
    }

    public long getSize() { return this.jobData.size(); }

    public List<String> getJobData() {
        return jobData;
    }

    public List<JobData> splitData(int splitSize) {
        List<JobData> splitData = new ArrayList<>();
        List<String> someData = new ArrayList<>();
        int dataSetSize = jobData.size();
        for (int i = 0; i < dataSetSize; i += splitSize) {
//            splitData.add(new ArrayList<>(
//                    jobData.subList(i, Math.min(dataSetSize, i + splitSize))
//            ));
            someData = jobData.subList(i, Math.min(dataSetSize, i + splitSize));
            splitData.add(new JobData(someData));
        }
        return splitData;
    }
}
