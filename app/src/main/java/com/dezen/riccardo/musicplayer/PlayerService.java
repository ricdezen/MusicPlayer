package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

import java.io.IOException;
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

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private SongManager songManager;
    private MediaPlayer mediaPlayer;
    private int currentSong = 0;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Media player.
        mediaPlayer = new MediaPlayer();

        // Ensure notification channel is created.
        NotificationHelper.getInstance(this).createChannelIfNeeded();

        // Setup PlaybackStateBuilder.
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE;
        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
                .setActions(actions);

        // Create MediaSession.
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        mediaSession.setCallback(new PlayerCallback(this));
        mediaSession.setSessionActivity(PendingIntent.getActivity(
                this, ACTIVITY_PENDING_INTENT_CODE,
                new Intent(this, MainActivity.class),
                0
        ));

        // Set the token for this Service. Allows finding the Session from outside.
        setSessionToken(mediaSession.getSessionToken());

        // Call the Song Manager.
        songManager = SongManager.getInstance(this);
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
     * Must be called after {@link PlayerService#play()}. Otherwise the MediaSession will have null
     * Metadata.
     *
     * @return The {@link Notification} for this Service, built through {@link NotificationHelper}.
     */
    public Notification getNotification() {
        return NotificationHelper.getInstance(this).getServiceNotification(this);
    }

    /**
     * @return The {@link MediaSessionCompat} for this Service.
     */
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    /**
     * Method to start playing the currently selected song.
     */
    public void play() {
        List<Song> songs = songManager.getSongs().getValue();
        if (songs == null) return;

        // TODO yo uhm like resume dude.
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            Song song = songs.get(currentSong);
            Log.d("PlayerService", "Playing song " + song.getTitle());
            mediaPlayer.setDataSource(this, song.getUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaSession.setPlaybackState(playbackStateBuilder.setState(
                    PlaybackStateCompat.STATE_PLAYING, 0, 0
            ).build());
            mediaSession.setMetadata(song.getMetadata());
        } catch (IOException e) {
            // TODO english bruh.
            Toast.makeText(this, "Impossibile riprodurre il file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Method to pause the playback of a song.
     */
    public void pause() {
        mediaPlayer.pause();
        mediaSession.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

    /**
     * Method to stop the playback of a song.
     */
    public void stopPlayer() {
        mediaPlayer.stop();
        mediaSession.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

    /**
     * When the Service is destroyed the media player is released.
     */
    @Override
    public void onDestroy() {
        stopPlayer();
        mediaSession.release();
        mediaPlayer.release();
        super.onDestroy();
    }
}
