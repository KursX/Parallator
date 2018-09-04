package com.kursx.parallator;

import java.io.File;
import java.util.Iterator;

//org.apache.commons.io
public class StringUtils {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final char UNIX_SEPARATOR = '/';
    public static final char EXTENSION_SEPARATOR = '.';
    public static final char WINDOWS_SEPARATOR = '\\';

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String join(Iterable<?> iterable, String separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return toString(first);
        }
        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    public static String wrap(String str, int wrapLength) {
        if (str == null) {
            return null;
        }
        if (wrapLength < 1) {
            wrapLength = 1;
        }
        int inputLineLength = str.length();
        int offset = 0;
        StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

        while (inputLineLength - offset > wrapLength) {
            if (str.charAt(offset) == ' ') {
                offset++;
                continue;
            }
            int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);
            if (spaceToWrapAt >= offset) {
                wrappedLine.append(str.substring(offset, spaceToWrapAt));
                wrappedLine.append(LINE_SEPARATOR);
                offset = spaceToWrapAt + 1;

            } else {
                spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
                if (spaceToWrapAt >= 0) {
                    wrappedLine.append(str.substring(offset, spaceToWrapAt));
                    wrappedLine.append(LINE_SEPARATOR);
                    offset = spaceToWrapAt + 1;
                } else {
                    wrappedLine.append(str.substring(offset));
                    offset = inputLineLength;
                }
            }
        }
        wrappedLine.append(str.substring(offset));
        return wrappedLine.toString();
    }

    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    public static String getBaseName(File file) {
        return removeExtension(getName(file.getAbsolutePath()));
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
