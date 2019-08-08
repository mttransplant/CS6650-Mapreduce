package MapReduce;

import java.util.List;

/**
 * an interface to represent the reduce function
 */
public interface Reducer {

    /**
     * Reduce function
     * @return
     */
    String reduce(String key, List<Integer> values);
}
