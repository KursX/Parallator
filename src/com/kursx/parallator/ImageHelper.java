package com.kursx.parallator;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {

    public static void copyFile(File from, File to ) throws IOException {
        FileInputStream inStream = new FileInputStream(from);
        FileOutputStream outStream = new FileOutputStream(to);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }
        inStream.close();
        outStream.close();
    }

    public static boolean checkImageSize(Image image) {
        return image.getHeight() == image.getWidth();
    }
}
