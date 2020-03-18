package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Objects;

/**
 * Class dedicated to ensuring the Notification channel is set up (if necessary) and building the
 * foreground Service's Notification.
 *
 * @author Riccardo De Zen.
 */
public class NotificationHelper {
    /**
     * Singleton instance.
     */
    private static NotificationHelper activeInstance;

    @NonNull
    private NotificationManager notificationManager;
    private Resources resources;

    /**
     * Private constructor. The Notification Manager must be available.
     *
     * @param context The calling {@link Context}, used to retrieve the
     *                {@link NotificationManager} and the app {@link Resources}.
     * @throws NullPointerException If the NotificationManager service is null (should never
     *                              happen).
     */
    private NotificationHelper(@NonNull Context context) {
        this.resources = context.getResources();
        // Should always be non null.
        this.notificationManager = (NotificationManager) Objects.requireNonNull(
                context.getSystemService(Context.NOTIFICATION_SERVICE)
        );
    }

    /**
     * Only way to instantiate the class.
     *
     * @param context The calling {@link Context}, used to retrieve the
     *                {@link NotificationManager} and the app {@link Resources}.
     * @return The only available instance of this class.
     */
    @NonNull
    public static NotificationHelper getInstance(@NonNull Context context) {
        if (activeInstance == null)
            activeInstance = new NotificationHelper(context);
        return activeInstance;
    }

    /**
     * Method to check whether the main notification channel has been registered. Must run on api
     * >= 26.
     *
     * @return {@code true} if the channel exists, {@code false} otherwise.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private boolean doesChannelExist() {
        String channelId = resources.getString(R.string.notification_channel_id);
        return notificationManager.getNotificationChannel(channelId) != null;
    }

    /**
     * Creates the channel if the notificationManager is not null. This method only hides the
     * actual private method that requires api level >= 26.
     */
    public void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        createChannel();
    }

    /**
     * Creates the channel if the notificationManager is not null. Requires api >= 26.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                resources.getString(R.string.notification_channel_id),
                resources.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(resources.getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Method used to create the notification for the foreground Service.
     * The {@link Notification} will be created without an associated channel if the api level is
     * < 26.
     * If the channel is not available, the default one is used.
     *
     * @param context The calling Context, used to create the {@link Notification.Builder}.
     */
    @NonNull
    public Notification getServiceNotification(@NonNull Context context) {
        Notification.Builder builder;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder = getLowApiBuilder(context);
        else
            builder = getChannelBuilder(context);

        builder
                .setContentTitle("Prova")
                .setContentText("Prova ancora di più");

        return builder.build();
    }

    /**
     * @param context The calling Context, used to create the {@link Notification.Builder}.
     * @return A {@link Notification.Builder} with no associated channel.
     */
    private Notification.Builder getLowApiBuilder(@NonNull Context context) {
        return new Notification.Builder(context);
    }

    /**
     * Method to create a Notification Builder with the appropriate channel, should only be
     * called on api >= 26. Will fail if the channel has not been registered.
     *
     * @param context The calling Context, used to create the {@link Notification.Builder}.
     * @return A {@link Notification.Builder} associated to the registered app channel.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification.Builder getChannelBuilder(@NonNull Context context) {
        return new Notification.Builder(
                context,
                resources.getString(R.string.notification_channel_id)
        );
    }

}
