package com.dezen.riccardo.musicplayer.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.dezen.riccardo.musicplayer.PlayerService;
import com.dezen.riccardo.musicplayer.R;

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
    private final NotificationManagerCompat notificationManager;
    private final Resources resources;

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
        this.notificationManager = NotificationManagerCompat.from(context);
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
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription(resources.getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Same as {@link NotificationManagerCompat#notify(int, Notification)}.
     *
     * @param id           Id for the Notification.
     * @param notification Notification to post.
     */
    public void notify(int id, @NonNull Notification notification) {
        notificationManager.notify(id, notification);
    }

    /**
     * Method used to create the notification for the foreground Service.
     * The {@link Notification} will be created without an associated channel if the api level is
     * < 26.
     * If the channel is not available, the default one is used.
     * <p>
     * The Notification will try to retrieve the MediaSession's metadata, so it will throw a
     * NullPointerException if the metadata is null. Update its metadata before calling this.
     * The Notification will have a play/pause button and skip/previous buttons.
     *
     * @param service The calling Service.
     * @throws NullPointerException If the MediaSession has no associated Metadata.
     */
    @NonNull
    public Notification getPlayerServiceNotification(@NonNull PlayerService service) throws NullPointerException {
        NotificationCompat.Builder builder = getNotificationBuilder(service);

        MediaControllerCompat controller = service.getMediaSession().getController();
        MediaMetadataCompat metadata = controller.getMetadata();
        MediaDescriptionCompat description = metadata.getDescription();

        // Icon depending on playback state.
        int toggleIcon =
                (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ?
                        R.drawable.pause_icon_24 : R.drawable.play_icon_24;

        builder
                .setPriority(NotificationCompat.PRIORITY_LOW)

                // Add the metadata for the currently playing track.
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(Utils.getThumbnail(metadata, service))

                // Enable launching the player by clicking the notification.
                .setContentIntent(controller.getSessionActivity())

                // Icon.
                .setSmallIcon(R.drawable.song_icon)

                // Make the transport controls visible on the lock screen.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Media control buttons
                .addAction(service.getModeDrawable(),
                        resources.getString(R.string.mode),
                        PendingIntent.getBroadcast(
                                service,
                                0,
                                new Intent(PlayerService.CYCLE_MODE),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        ))
                .addAction(R.drawable.previous_icon_24,
                        resources.getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        ))
                .addAction(
                        toggleIcon,
                        resources.getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        ))
                .addAction(R.drawable.next_icon_24,
                        resources.getString(R.string.next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        ))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.getMediaSession().getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                );


        return builder.build();
    }

    /**
     * @param context The calling Context, used to retrieve the Notification builder.
     * @return An appropriate Notification builder based on api version.
     */
    private NotificationCompat.Builder getNotificationBuilder(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return getLowApiBuilder(context);
        else
            return getChannelBuilder(context);
    }

    /**
     * @param context The calling Context, used to create the {@link Notification.Builder}.
     * @return A {@link Notification.Builder} with no associated channel.
     */
    private NotificationCompat.Builder getLowApiBuilder(@NonNull Context context) {
        return new NotificationCompat.Builder(context);
    }

    /**
     * Method to create a Notification Builder with the appropriate channel, should only be
     * called on api >= 26. Will fail if the channel has not been registered.
     *
     * @param context The calling Context, used to create the {@link Notification.Builder}.
     * @return A {@link Notification.Builder} associated to the registered app channel.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationCompat.Builder getChannelBuilder(@NonNull Context context) {
        return new NotificationCompat.Builder(
                context,
                resources.getString(R.string.notification_channel_id)
        );
    }

}
