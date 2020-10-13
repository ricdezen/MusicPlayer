package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.dezen.riccardo.musicplayer.song.SongManager;

import java.util.List;

/**
 * Service that manages the playback of songs.
 *
 * @author Riccardo De Zen.
 */
public class PlayerService extends MediaBrowserServiceCompat {

    public static final String LOG_TAG = "PlayerService";
    public static final int NOTIFICATION_ID = 1234;

    private static final int ACTIVITY_PENDING_INTENT_CODE = 4321;

    private NotificationHelper notificationHelper;
    private MediaSessionCompat mediaSession;
    private SongManager songManager;
    private PlayerWrapper player;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Call the Song Manager.
        songManager = SongManager.getInstance(this);

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
        player = new PlayerWrapper(songManager, this);
        mediaSession.setCallback(player);


        // Set the token for this Service. Allows finding the Session from outside.
        setSessionToken(mediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
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
     * @return The {@link MediaSessionCompat} for this Service.
     */
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    /**
     * When the Service is destroyed the media player is released.
     */
    @Override
    public void onDestroy() {
        player.release();
        mediaSession.release();
        super.onDestroy();
    }
}
