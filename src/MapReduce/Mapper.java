package MapReduce;

import java.util.Map;

/**
 * an interface to represent the map function
 */
public interface Mapper {
    /**
     * Map function
     * @param line input
     * @param map an aggregate map
     */
    void map(String line, Map<String, Integer> map);


    /**
     * Partition function
     * @param key Mapper key
     * @return partition key
     */
    int getPartition(String key);
}
