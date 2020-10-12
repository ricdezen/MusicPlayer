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
    private static final long[] SUPPORTED_ACTIONS = new long[]{
            PlaybackStateCompat.ACTION_PLAY_PAUSE,
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    };

    private NotificationHelper notificationHelper;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private SongManager songManager;
    private MediaPlayer mediaPlayer;
    private String currentSongId;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Media player.
        mediaPlayer = new MediaPlayer();

        // Ensure notification channel is created.
        notificationHelper = NotificationHelper.getInstance(this);
        notificationHelper.createChannelIfNeeded();

        // Setup PlaybackStateBuilder.
        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
                .setActions(Utils.bitOR(SUPPORTED_ACTIONS));

        // Create MediaSession.
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        mediaSession.setCallback(playerCallback);
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
     * Prepare new song and play.
     *
     * @param song The song to play.
     */
    public void play(Song song) {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        currentSongId = song.getId();
        prepare(song);
        resume();
    }

    /**
     * Method to prepare a song to play. Resets the current mediaPlayer.
     *
     * @param song The song for which to prepare playback.
     */
    public void prepare(Song song) {
        mediaPlayer.reset();
        try {
            Log.d("PlayerService", "Playing song " + song.getTitle());
            mediaPlayer.setDataSource(this, song.getUri());
            mediaPlayer.prepare();
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
     * Resume playback without changing the song.
     */
    public void resume() {
        mediaPlayer.start();
        mediaSession.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
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

    /**
     * Callback class implementing the various methods for communicating between the
     * {@link PlayerService} and a client.
     */
    private MediaSessionCompat.Callback playerCallback = new MediaSessionCompat.Callback() {

        /**
         * Play from a specific media id. If the id is not found in the SongManager class, the
         * method returns.
         *
         * @param mediaId Id, String containing the index of the item in the list.
         * @param extras Ignored.
         */
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            // Return if song is not found.
            Song song = songManager.get(mediaId);
            if (song == null)
                return;

            // Start (or restart) the Service.
            startService(new Intent(PlayerService.this, PlayerService.class));

            // Set the Media Session as active.
            mediaSession.setActive(true);

            // Play the song on the Service.
            play(song);

            // Put the Service in the foreground.
            startForeground(
                    NOTIFICATION_ID,
                    getNotification()
            );

        }

        /**
         * The Media session received a play command.
         * TODO : Audio Focus, noisy
         */
        @Override
        public void onPlay() {
            super.onPlay();

            // Start (or restart) the Service.
            startService(new Intent(PlayerService.this, PlayerService.class));

            // Set the Media Session as active.
            mediaSession.setActive(true);

            // Play the song on the Service.
            resume();

            // Put the Service in the foreground.
            startForeground(
                    NOTIFICATION_ID,
                    getNotification()
            );
        }

        /**
         * The Media session received a pause command.
         * TODO noisy
         */
        @Override
        public void onPause() {
            super.onPause();
            pause();
            // Stop being in the foreground.
            stopForeground(false);
            // Update the notification.
            notificationHelper.notify(NOTIFICATION_ID, getNotification());
        }

        /**
         * The Media session received a stop command.
         */
        @Override
        public void onStop() {
            super.onStop();
            stopPlayer();
            mediaSession.setActive(false);
            stopForeground(false);
            stopSelf();
        }

        /**
         * Skip to the next Song.
         */
        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            if (currentSongId == null)
                return;

            Song song = songManager.next(currentSongId);
            if (song == null)
                return;

            onPlayFromMediaId(song.getId(), null);
        }

        /**
         * Skip to the previous Song.
         */
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            if (currentSongId == null)
                return;

            Song song = songManager.previous(currentSongId);
            if (song == null)
                return;

            onPlayFromMediaId(song.getId(), null);
        }
    };
}
