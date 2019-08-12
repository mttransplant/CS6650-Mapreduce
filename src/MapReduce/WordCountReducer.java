package MapReduce;

import java.util.List;
import java.util.Map;

public class WordCountReducer implements Reducer {
    @Override
    public void reduce(List<Map.Entry<String, Integer>> values, Map<String, Integer> map) {
        for (Map.Entry pair: values) {
            map.put((String)pair.getKey(), map.getOrDefault(pair.getKey(), 0) + (int) pair.getValue());
        }
    }
}
