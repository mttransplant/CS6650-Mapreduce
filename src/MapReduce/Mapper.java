package MapReduce;

import java.io.Serializable;
import java.util.Map;

/**
 * an interface to represent the map function
 */
public interface Mapper extends Serializable {
    /**
     * Map function
     * @param line input
     * @param map an aggregate map
     */
    void map(String line, Map<String, Integer> map);


    /**
     * Partition function: Returns the partition id of where a particular map result should be sent.
     * @param key Mapper key
     * @return partition key
     */
    int getPartition(String key, int numReducers);
}
