/**
 * an interface to represent the DataSet that will be passed from a peer to a JobManager
 * and from a JobManager to a TaskManager
 */

public interface DataSet {
    DataSet getDataset();
}
