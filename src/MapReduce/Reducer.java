package MapReduce;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * an interface to represent the reduce function
 */
public interface Reducer extends Serializable {

    /**
     * Reduce function: take the values from the mapper function and aggregates according to the reducer function.
     * @param values values from the mapper
     * @param map aggregate map
     */
    void reduce(List<KeyValuePair> values, Map<String, Integer> map);
}