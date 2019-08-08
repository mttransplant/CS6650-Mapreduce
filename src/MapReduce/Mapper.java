package MapReduce;

/**
 * an interface to represent the map function
 */
public interface Mapper {
    /**
     * Map function
     * @param line input line
     * @return key-value pair
     */
    Pair map(String line);


    /**
     * Partition function
     * @return
     */
    int getPartition(Pair pair);
}
