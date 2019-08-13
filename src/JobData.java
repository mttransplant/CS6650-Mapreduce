import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// this class stores the data that is used for a particular job and the methods used to describe and retrieve the enclosed data
public class JobData implements Serializable {
    private List<String> jobData; // the raw data that will be used by the job

    // this initializer is used by the User to provide raw data
    JobData(String[] stringArray) {
        this.jobData = new ArrayList<>(stringArray.length);

        for (String s : stringArray) {
            this.jobData.add(s);
        }
    }

    // method used to describe the size of the data. This can be used by the JobManager to determine how many
    // TaskManagers to request.
    public long getSize() { return this.jobData.size(); }

    // method to retrieve the data package of this object.
    public List<String> getJobData() {
        return this.jobData;
    }

    // method used to return a list of "splitSize" subsets of this object's data. Used to split up the data
    // so it can be distributed to the task managers.
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