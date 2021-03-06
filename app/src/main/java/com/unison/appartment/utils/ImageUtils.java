package com.unison.appartment.utils;

import android.graphics.Bitmap;

public class ImageUtils {
    // Costanti usate per indicare se l'immagine considerata è tonda o quadrata
    // Serve quando un'immagine viene aperta perché il codice è diveso
    public final static String IMAGE_TYPE_ROUND = "0";
    public final static String IMAGE_TYPE_SQUARE = "1";

    public final static int MAX_WIDTH = 680;
    public final static int MAX_HEIGHT = 680;

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
