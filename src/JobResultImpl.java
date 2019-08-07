public class JobResultImpl implements JobResult {
    private String status;
    private DataSet dataSet;

    public void JobResult(String status, DataSet dataset) {
        this.status = status;
        this.dataSet = dataSet;
    }

    public String getStatus() {
        return this.status;
    }

    public DataSet getResults() {
        return this.dataSet;
    }
}
