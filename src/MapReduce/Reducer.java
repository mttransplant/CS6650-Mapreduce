package MapReduce;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * an interface to represent the reduce function
 */
public interface Reducer extends Serializable {

    /**
     * Reduce function'
     * @param values values from the mapper
     * @param map aggregate map
     */
    void reduce(List<Pair> values, Map<String, Integer> map);
}
