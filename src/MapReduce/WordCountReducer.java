package MapReduce;

import java.util.List;
import java.util.Map;

public class WordCountReducer implements Reducer {
    @Override
    public void reduce(List<Pair> values, Map<String, Integer> map) {
        for (Pair pair: values) {
            map.put((String)pair.key, map.getOrDefault(pair.key, 0) + (int) pair.value);
        }
    }
}
