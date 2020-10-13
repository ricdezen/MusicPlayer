package com.dezen.riccardo.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private static int defaultImage = R.drawable.song_icon;

    /**
     * Retrieve a Drawable for a certain metadata. If the metadata is null, retrieve a default
     * Drawable.
     *
     * @param metadata        Metadata for a song.
     * @param contentResolver The Content Resolver.
     * @param resources       App resources.
     * @return The Bitmap for the given song, or a default image.
     */
    public static Drawable getMediaDrawable(@Nullable MediaMetadataCompat metadata,
                                            @NonNull ContentResolver contentResolver,
                                            @NonNull Resources resources) {
        if (metadata == null)
            return getDefaultMediaDrawable(resources);

        Uri uri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
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
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
            return new BitmapDrawable(resources, bitmap);
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
            // No image found for whatever reason, fall back to a default.
            return getDefaultMediaDrawable(resources);
        }
    }

    /**
     * Retrieve a Drawable for a certain metadata. If the metadata is null, retrieve a default
     * Drawable.
     *
     * @param metadata Metadata for a song.
     * @param context  The context.
     * @return The Bitmap for the given song, or a default image.
     */
    public static Drawable getMediaDrawable(@Nullable MediaMetadataCompat metadata,
                                            @NonNull Context context) {
        return getMediaDrawable(metadata, context.getContentResolver(), context.getResources());
    }

    /**
     * Retrieve a Bitmap for a certain metadata. If the metadata is null, retrieve a default
     * Bitmap.
     *
     * @param metadata        Metadata for a song.
     * @param contentResolver The Content Resolver.
     * @param resources       App resources.
     * @return The Bitmap for the given song, or a default image.
     */
    public static Bitmap getMediaBitmap(@Nullable MediaMetadataCompat metadata,
                                        @NonNull ContentResolver contentResolver,
                                        @NonNull Resources resources) {
        if (metadata == null)
            return getDefaultMediaBitmap(resources);

        Uri uri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
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
            return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
            // No image found for whatever reason, fall back to a default.
            return getDefaultMediaBitmap(resources);
        }
    }

    /**
     * Retrieve a Bitmap for a certain metadata. If the metadata is null, retrieve a default
     * Bitmap.
     *
     * @param metadata Metadata for a song.
     * @param context  The context.
     * @return The Bitmap for the given song, or a default image.
     */
    public static Bitmap getMediaBitmap(@Nullable MediaMetadataCompat metadata,
                                        @NonNull Context context) {
        return getMediaBitmap(metadata, context.getContentResolver(), context.getResources());
    }

    /**
     * Change the default song drawable's id.
     *
     * @param defaultImage The resource id of the desired default drawable.
     */
    public static void setDefaultImage(int defaultImage) {
        Utils.defaultImage = defaultImage;
    }

    /**
     * Return the default Drawable for a Song.
     *
     * @param resources The app resources.
     * @return The default Drawable.
     */
    private static Drawable getDefaultMediaDrawable(@NonNull Resources resources) {
        return ResourcesCompat.getDrawable(resources, defaultImage, null);
    }

    /**
     * Return the default Bitmap for a Song.
     *
     * @param resources The app resources.
     * @return The default Bitmap.
     */
    private static Bitmap getDefaultMediaBitmap(@NonNull Resources resources) {
        Drawable drawable = getDefaultMediaDrawable(resources);
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
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
