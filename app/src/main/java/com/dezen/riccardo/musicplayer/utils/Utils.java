package com.dezen.riccardo.musicplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Size;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.dezen.riccardo.musicplayer.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private static final int DEFAULT_IMAGE = R.drawable.song_icon;
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;


    private static Drawable defaultDrawable;
    private static Bitmap defaultBitmap;

    /**
     * Retrieve a Bitmap for a certain metadata. If the metadata is null, retrieve a default
     * Bitmap. The bitmap is full size, can be used as a full artwork.
     *
     * @param metadata        Metadata for a song.
     * @param contentResolver The Content Resolver.
     * @param resources       App resources.
     * @return The Bitmap for the given song, or a default image.
     */
    public static synchronized Bitmap getThumbnail(@Nullable MediaMetadataCompat metadata,
                                                   @NonNull ContentResolver contentResolver,
                                                   @NonNull Resources resources) {
        if (metadata == null)
            return getDefaultThumbnail(resources);

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
            asset.close();

            // If no embedded picture is found, the array is null.
            if (rawBytes == null)
                throw new NullPointerException();

            return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
        } catch (IOException | RuntimeException e) {
            // Could not find file.
            return getDefaultThumbnail(resources);
        }
    }

    /**
     * Retrieve a Bitmap for a certain metadata. If the metadata is null, retrieve a default
     * Bitmap. The bitmap is full size, can be used as a full artwork.
     *
     * @param metadata Metadata for a song.
     * @param context  The context.
     * @return The Bitmap for the given song, or a default image.
     */
    public static synchronized Bitmap getThumbnail(@Nullable MediaMetadataCompat metadata,
                                                   @NonNull Context context) {
        return getThumbnail(metadata, context.getContentResolver(), context.getResources());
    }

    /**
     * Return the default Drawable for a Song. It is a Vector drawable, so it can be scaled as much
     * as needed.
     *
     * @param resources The app resources.
     * @return The default Drawable.
     */
    public static Drawable getDefaultArtwork(@NonNull Resources resources) {
        if (defaultDrawable == null)
            defaultDrawable = ResourcesCompat.getDrawable(resources, DEFAULT_IMAGE, null);
        return defaultDrawable;
    }

    /**
     * Return the default Bitmap for a Song. It is a 128dp by 128dp image. It is meant for
     * thumbnails, for full size retrieve the artwork with
     * {@link Utils#getDefaultArtwork(Resources)}.
     *
     * @param resources The app resources.
     * @return The default Bitmap.
     */
    public static Bitmap getDefaultThumbnail(@NonNull Resources resources) {
        if (defaultBitmap == null) {
            Drawable drawable = getDefaultArtwork(resources);
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            defaultBitmap = bitmap;
        }
        return defaultBitmap;
    }

    /**
     * Returns a Bitmap, resized to be displayed as a squared, cropped thumbnail. It will resize
     * keeping aspect ratio, the resulting image will have {@code size} on its smaller dimension,
     * using an ImageView with {@code android:scaleType="centerCrop"} will crop the image on its
     * larger dimension.
     *
     * @param thumbnail The image to resize.
     * @param size      The size the smaller side should have after resizing.
     * @return The resized Bitmap.
     */
    public static Bitmap resizeThumbnail(@NonNull Bitmap thumbnail, Size size) {
        int width = thumbnail.getWidth();
        int height = thumbnail.getHeight();
        int newWidth, newHeight;
        double aspectRatio = (1.0 * width) / (1.0 * height);
        if (width > height) {
            newHeight = size.getHeight();
            newWidth = (int) Math.round(size.getWidth() * aspectRatio);
        } else {
            newWidth = size.getWidth();
            newHeight = (int) Math.round(size.getHeight() * aspectRatio);
        }
        return Bitmap.createScaledBitmap(thumbnail, newWidth, newHeight, true);
    }

    /**
     * Get size for the Thumbnail.
     *
     * @param imageView ImageView containing Thumbnail.
     * @return Either the size of the ImageView or a default 128 by 128 if the ImageView is 0 in
     * either width or height.
     */
    public static Size getThumbnailSize(@NonNull ImageView imageView) {
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        if (width == 0 || height == 0)
            return new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        return new Size(width, height);
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

    /**
     * @return An icon for a play button if the state is not playing, an icon for a pause button if
     * the state is playing.
     */
    public static int getButtonIcon(int playbackState) {
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            return R.drawable.pause_icon;
        return R.drawable.play_icon;
    }

    /**
     * Run something in the main thread.
     *
     * @param runnable The "something" to run.
     */
    public static void onMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * Run something in the main thread.
     *
     * @param runnable The "something" to run.
     * @param delay    Delay in milliseconds.
     */
    public static void onMainThread(@NonNull Runnable runnable, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delay);
    }

}
