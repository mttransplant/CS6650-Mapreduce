package MapReduce;

import java.util.Map;
import java.util.StringTokenizer;

public class WordCountMapper implements Mapper {
    @Override
    public void map(String line, Map<String, Integer> map) {
        StringTokenizer itr = new StringTokenizer(line);
        String word;
        while (itr.hasMoreTokens()) {
            word = itr.nextToken();
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
    }

    @Override
    public int getPartition(String key) {
        char letter = key.toLowerCase().charAt(0);
        return letter - 'a';
    }
}
