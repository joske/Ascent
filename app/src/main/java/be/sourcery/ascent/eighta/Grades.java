package be.sourcery.ascent.eighta;

import java.util.HashMap;

/**
 * Created by jos on 16/02/17.
 */

public class Grades {

    private static HashMap<String, String> frenchTo8a = new HashMap<>();

    static {
        frenchTo8a.put("3", "10");
        frenchTo8a.put("4", "11");
        frenchTo8a.put("5a", "12");
        frenchTo8a.put("5b", "13");
        frenchTo8a.put("5c", "14");
        frenchTo8a.put("6a", "15");
        frenchTo8a.put("6a+", "16");
        frenchTo8a.put("6b", "17");
        frenchTo8a.put("6b+", "18");
        frenchTo8a.put("6c", "19");
        frenchTo8a.put("6c+", "20");
        frenchTo8a.put("7a", "21");
        frenchTo8a.put("7a+", "22");
        frenchTo8a.put("7b", "23");
        frenchTo8a.put("7b+", "24");
        frenchTo8a.put("7c", "25");
        frenchTo8a.put("7c+", "26");
        frenchTo8a.put("8a", "27");
        frenchTo8a.put("8a+", "28");
        frenchTo8a.put("8b", "29");
        frenchTo8a.put("8b+", "30");
        frenchTo8a.put("8c", "31");
        frenchTo8a.put("8c+", "32");
        frenchTo8a.put("9a", "33");
        frenchTo8a.put("9a+", "34");
        frenchTo8a.put("9b", "35");
        frenchTo8a.put("9b+", "36");
        frenchTo8a.put("9c", "37");

    }

    public static String convertFrenchTo8a(String french) {
        return frenchTo8a.get(french);
    }

}
