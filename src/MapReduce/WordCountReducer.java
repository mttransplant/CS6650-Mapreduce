package MapReduce;

import java.util.List;
import java.util.Map;

public class WordCountReducer implements Reducer {
    @Override
    public void reduce(List<KeyValuePair> values, Map<String, Integer> map) {
        for (KeyValuePair pair: values) {
            map.put(pair.getKey(), map.getOrDefault(pair.getKey(), 0) + pair.getValue());
        }
    }
}