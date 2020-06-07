package com.kursx.parallator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartsSeparator {

    public static List<String> getParts(String input, boolean fullSeparating, String dividers) {
        int minimum = fullSeparating ? 0 : 25;
        String data = input;
        try {
            ArrayList<Integer> dividersIndexes = new ArrayList<>();
            int startIndex = 0;
            while (true) {

                int index = indexOf(data, fullSeparating, dividers);
                if (index == -1) {
                    break;
                }
                startIndex += index;
                dividersIndexes.add(startIndex);
                startIndex += 1;
                data = data.substring(index + 1);

            }
            ArrayList<Integer> indexesForSave = new ArrayList<>();
             if (dividersIndexes.isEmpty()) {
                 return Collections.singletonList(input);
            } else {
                 ArrayList<String> parts = new ArrayList<>();
                 int firstIndex = 0;
                for (int dividersIndex : dividersIndexes) {
                    String part = input.substring(firstIndex, dividersIndex + 1).trim();
                    if (putPartToList(part, parts, minimum)) {
                        indexesForSave.add(dividersIndex);
                    }
                    firstIndex = dividersIndex + 1;
                }

                String part = input.substring(firstIndex).trim();
                putPartToList(part, parts, minimum);
                 return parts;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return Collections.singletonList(input);
    }

    static int indexOf(String s, boolean full, String dividers) {
        Matcher matcher;
        if (dividers != null) {
            matcher = Pattern.compile(dividers).matcher(s);
        } else {
            matcher = (full ? fullP : p).matcher(s);
        }
        return matcher.find() ? matcher.start() : -1;
    }

    private static Boolean putPartToList(String part, List<String> parts, int minimum) {
        if (part.isEmpty()) return false;
        int size = parts.size();
        if (part.length() < minimum && size > 0 && (parts.get(size - 1).length() < minimum || part.length() < 2)
                || size > 0 && checkNonDividing(parts.get(size - 1))
                || size > 0 && parts.get(size - 1).length() < minimum) {
            parts.set(size - 1, parts.get(size - 1) + (part.length() > 1 ? " " : "") + part);
            return false;
        } else {
            parts.add(part);
            return true;
        }
    }

    static boolean checkNonDividing(String previos) {
        for (String example : new String[]{"mr.", "mrs.", " p."}) {
            if (previos.toLowerCase().endsWith(example)) {
                return true;
            }
        }
        return false;
    }


    static Pattern p = Pattern.compile("[.…!?][ \\w\n]");
    static Pattern fullP = Pattern.compile("[.…!?]");
    static Pattern userP = Pattern.compile("[.…!?]");
}
