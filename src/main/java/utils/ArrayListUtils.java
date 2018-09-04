package utils;

import java.util.ArrayList;

/**
 * Useful functions for ArrayList manipulation
 * @author aylai2
 */
public class ArrayListUtils {

    public static String stringListToString(ArrayList<String> strList, String delimiter) {
        String str = "";
        if (strList.size() == 0) {
            return str;
        }
        for (String s : strList) {
            str += s + delimiter;
        }
        str = str.substring(0, str.length()-1);
        return str;
    }

    public static ArrayList<String> intListToStrList(ArrayList<Integer> intList) {
        ArrayList<String> strList = new ArrayList<>();
        for (int i : intList) {
            strList.add(Integer.toString(i));
        }
        return strList;
    }

    public static String intListToString(ArrayList<Integer> intList, String delimiter) {
        ArrayList<String> strList = intListToStrList(intList);
        return stringListToString(strList, delimiter);
    }
}
