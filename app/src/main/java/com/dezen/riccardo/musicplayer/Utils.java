package com.dezen.riccardo.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private static int defaultImage = R.drawable.song_icon;

    /**
     * @param context  The context, used to get resources.
     * @param metadata Metadata for a song.
     * @return The Bitmap for the given song, or a default image.
     */
    public static Bitmap getBitmap(@NonNull Context context,
                                   @NonNull MediaMetadataCompat metadata) {
        Resources resources = context.getResources();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        Bitmap bitmap;
        try {
            // If the content resolver has crashed, the file is null.
            ParcelFileDescriptor asset = contentResolver.openFileDescriptor(uri, "r");
            if (asset == null)
                throw new FileNotFoundException();

            // Need a raw file descriptor.
            FileDescriptor file = asset.getFileDescriptor();
            retriever.setDataSource(file);
            byte[] rawBytes = retriever.getEmbeddedPicture();

            // If no embedded picture is found, the array is null.
            if (rawBytes == null)
                throw new NullPointerException();
            bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);

        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
            // No image found for whatever reason, fall back to a default.
            Drawable originalImage = resources.getDrawable(defaultImage);
            bitmap = Bitmap.createBitmap(
                    originalImage.getIntrinsicWidth(),
                    originalImage.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            originalImage.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            originalImage.draw(canvas);
        }
        return bitmap;
    }

    /**
     * Return the bitwise XOR for an array of long arguments.
     *
     * @param args The arguments.
     * @return The bitwise XOR for the arguments.
     */
    public static long bitOR(long... args) {
        long or = 0;
        for (long n : args)
            or = or | n;
        return or;
    }

    /**
     * Given two arrays of the same size, returns a map of each element of the first array to the
     * corresponding element of the second. If values is longer than keys, the trailing elements are
     * ignored, if values is shorter an Exception is thrown.
     *
     * @param keys   An array of keys.
     * @param values An array of values.
     * @return A Map of the values in the first array to the values in the second.
     * @throws IndexOutOfBoundsException If values is shorter than keys.
     */
    @NonNull
    public static <K, V> Map<K, V> toMap(K[] keys, V[] values) throws IndexOutOfBoundsException {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++)
            map.put(keys[i], values[i]);
        return map;
    }

}
