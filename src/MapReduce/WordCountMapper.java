package MapReduce;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class is the implementation of a type of Mapper
 * This particular Mapper function counts the words in a string of text
 */
public class WordCountMapper implements Mapper {
    // default initializer
    @Override
    public void map(String line, Map<String, Integer> map) {
        StringTokenizer itr = new StringTokenizer(line);
        String word;
        while (itr.hasMoreTokens()) {
            word = itr.nextToken();
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
    }

    // method that will return a reducer partition id based on the desired key and the number of reducers being used
    @Override
    public int getPartition(String key, int numReducers) {
        char letter = key.toLowerCase().charAt(0);
        return letter % numReducers;
    }
}
