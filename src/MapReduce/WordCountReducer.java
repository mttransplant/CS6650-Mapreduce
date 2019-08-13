package MapReduce;

import java.util.List;
import java.util.Map;

/**
 * This class is a type of Reducer
 * This particular reducer works through a word count map and consolidates the counts for each word
 * so that any word is found as one value entry in the resulting map
 */

public class WordCountReducer implements Reducer {
    // default initializer and process
    @Override
    public void reduce(List<KeyValuePair> values, Map<String, Integer> map) {
        for (KeyValuePair pair: values) {
            map.put(pair.getKey(), map.getOrDefault(pair.getKey(), 0) + pair.getValue());
        }
    }
}