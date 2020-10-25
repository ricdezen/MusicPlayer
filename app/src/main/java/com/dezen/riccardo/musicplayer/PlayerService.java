package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.dezen.riccardo.musicplayer.utils.NotificationHelper;

import java.util.List;

/**
 * Service that manages the playback of songs.
 *
 * @author Riccardo De Zen.
 */
public class PlayerService extends MediaBrowserServiceCompat {

    public static final String CYCLE_MODE = PlayerService.class.getName() + ".CYCLE_MODE";
    private static final int[] MODE_ICON = {
            R.drawable.no_repeat,
            R.drawable.repeat_one_icon,
            R.drawable.repeat_all_icon,
            R.drawable.shuffle_icon
    };

    public static final String LOG_TAG = "PlayerService";
    public static final int NOTIFICATION_ID = 1234;
    private static final int ACTIVITY_PENDING_INTENT_CODE = 4321;

    private final CycleModeReceiver receiver = new CycleModeReceiver();
    private NotificationHelper notificationHelper;
    private MediaSessionCompat mediaSession;
    private PlayerWrapper player;

    // Methods to trigger the various modes in the player.
    private final Runnable[] MODES = {
            () -> player.noRepeat(),
            () -> player.repeatOne(),
            () -> player.repeatAll(),
            () -> player.shuffle()
    };

    private int currentMode = 0;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Ensure notification channel is created.
        notificationHelper = NotificationHelper.getInstance(this);
        notificationHelper.createChannelIfNeeded();

        // Create MediaSession.
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setSessionActivity(PendingIntent.getActivity(
                this, ACTIVITY_PENDING_INTENT_CODE,
                new Intent(this, MainActivity.class),
                0
        ));
        // Set the token for this Service. Allows finding the Session from outside.
        setSessionToken(mediaSession.getSessionToken());
        player = new PlayerWrapper(this);
        mediaSession.setCallback(player);

        // Receiver to change mode.
        registerReceiver(receiver, new IntentFilter(CYCLE_MODE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * When the Service is destroyed:
     * - MediaPlayer is released.
     * - MediaSession is released.
     * - SongManager associated to the session is freed.
     * - The Service removes its notification from the foreground.
     */
    @Override
    public void onDestroy() {
        player.release();
        mediaSession.release();
        stopForeground(true);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * TODO not yet implemented, song list is provided via other classes.
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 @Nullable Bundle rootHints) {
        return new BrowserRoot("HELLO_I_AM_TEMPORARY", null);
    }

    /**
     * TODO not yet implemented, song list is provided via other classes.
     */
    @Override
    public void onLoadChildren(@NonNull String parentId,
                               @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    /**
     * Method to retrieve the Notification for this Service.
     *
     * @return The {@link Notification} for this Service, built through {@link NotificationHelper}.
     */
    public Notification getNotification() {
        return notificationHelper.getPlayerServiceNotification(this);
    }

    /**
     * @return The appropriate Drawable id for the current mode.
     */
    public int getModeDrawable() {
        return MODE_ICON[currentMode];
    }

    /**
     * @return The {@link MediaSessionCompat} for this Service.
     */
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    /**
     * Cycle through the 4 possible modes for playback.
     */
    private void nextMode() {
        int nextMode = (currentMode + 1) % MODES.length;
        // Run the method in the player.
        MODES[nextMode].run();
        // Update mode.
        currentMode = nextMode;
        // Update notification.
        notificationHelper.notify(NOTIFICATION_ID, getNotification());
    }

    private class CycleModeReceiver extends BroadcastReceiver {

        /**
         * @param context The calling Context.
         * @param intent  The Action. Only accepted type is {@link PlayerService#CYCLE_MODE}.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(CYCLE_MODE))
                return;
            nextMode();
        }
    }

}
