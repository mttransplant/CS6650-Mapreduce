import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobData implements Serializable {
    private List<String> jobData;

    // this initializer is used by the User to provide raw data
    JobData(String[] stringArray) {
        this.jobData = new ArrayList<>(stringArray.length);

        for (String s : stringArray) {
            this.jobData.add(s);
        }
    }

    public long getSize() { return this.jobData.size(); }

    public List<String> getJobData() {
        return this.jobData;
    }

    public List<JobData> splitData(int splitSize) {
        List<JobData> splitData = new ArrayList<>();

        int dataSetSize = this.jobData.size();

        for (int i = 0; i < dataSetSize; i += splitSize) {
            List<String> someData = this.jobData.subList(i, Math.min(dataSetSize, i + splitSize));
            String[] splitArray = new String[someData.size()];

            for (int j = 0; j < splitArray.length; j++) {
                splitArray[j] = someData.get(j);
            }

            splitData.add(new JobData(splitArray));
        }

        return splitData;
    }
}