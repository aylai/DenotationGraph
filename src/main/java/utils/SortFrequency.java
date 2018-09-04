package utils;

import java.util.*;

/**
 * Created by alai on 9/5/15.
 */
public class SortFrequency {

    public static ArrayList<String> getSortedFrequency(HashMap<String,Integer> counts) {
        ArrayList<Map.Entry<String, Integer>> vocabList = new ArrayList<>(counts.entrySet());
        Collections.sort(vocabList, new VocabComparator());
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, Integer> e : vocabList) {
            keys.add(e.getKey());
        }
        return keys;
    }

    public static class VocabComparator implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
            int result;
            Integer value1 = e1.getValue();
            Integer value2 = e2.getValue();
            if (value1.compareTo(value2) == 0) {
                String word1 = e1.getKey();
                String word2 = e2.getKey();
                result = word1.compareToIgnoreCase(word2);
            }
            else {
                result = value2.compareTo(value1);
            }
            return result;
        }
    }
}
