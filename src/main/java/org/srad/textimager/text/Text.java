package main.java.org.srad.textimager.text;

import java.util.Arrays;
import java.util.HashSet;

public class Text {
    public static String normalize(String text) {
        return text.trim().toLowerCase().replace("\\s+"," ");
    }

    public static String[] tokenize(String text) {
        return text.split(" ");
    }

    public static HashSet<String> uniqueNormalizedTokens(String text) {
        return new HashSet<String>(Arrays.asList(tokenize(normalize(text))));
    }
}
