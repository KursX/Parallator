package com.kursx.parallator;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

    public static final String DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static void exception(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        e.printStackTrace();
        File workingDirectory = new File(System.getProperty("user.dir"));
        try {
            FileChannel fileChannel = new RandomAccessFile(
                    new File(workingDirectory, ".log"), "rw").getChannel();
            fileChannel.position(fileChannel.size());
            fileChannel.write(ByteBuffer.wrap((getDateTime() + " " + sw.toString() + "\n\n").getBytes()));
            fileChannel.close();
        } catch (IOException exc) {
            e.printStackTrace();
        }
    }

    public static String getDateTime() {
        return format(new Date(), DATE_YYYY_MM_DD_HH_MM_SS);
    }

    private static String format(final Date date, String template) {
        if (date != null && template != null) {
            try {
                return format(template).format(date);
            } catch (final NumberFormatException e) {
                Logger.exception(e);
                return "";
            }
        }
        return "";
    }

    public static SimpleDateFormat format(String template) {
        return new SimpleDateFormat(template, Locale.getDefault());
    }
}
