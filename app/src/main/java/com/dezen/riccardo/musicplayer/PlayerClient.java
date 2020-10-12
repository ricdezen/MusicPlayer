package com.dezen.riccardo.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.song.Song;

/**
 * Class used to simplify MediaBrowserClient usage. Communicates with a {@link PlayerService}. It
 * instantiates the Service automatically and triggers a callback when the Service is connected, use
 * it to know when you can enable UI controls.
 * {@link PlayerClient#connect()} should be called when the host Activity is started.
 * {@link PlayerClient#disconnect()} should be called when the Activity is stopped.
 *
 * @author Riccardo De Zen.
 */
public class PlayerClient {

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat.ConnectionCallback callbacks;

    /**
     * @param activity The {@link Activity} hosting the media player.
     */
    public PlayerClient(@NonNull Activity activity) {
        // Defining callbacks.
        callbacks = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                // Will be non-null when this method is called.
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                // Initialize the media controller.
                mediaController = new MediaControllerCompat(
                        activity, token
                );
                MediaControllerCompat.setMediaController(activity, mediaController);
            }

            @Override
            public void onConnectionFailed() {
                super.onConnectionFailed();
                Toast.makeText(activity, R.string.connection_failed_error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectionSuspended() {
                super.onConnectionSuspended();
                Toast.makeText(activity, R.string.service_shut_down_warning, Toast.LENGTH_LONG).show();
            }
        };

        // Initialize MediaBrowser.
        mediaBrowser = new MediaBrowserCompat(
                activity,
                new ComponentName(activity, PlayerService.class),
                callbacks,
                null
        );
    }

    /**
     * Connect to the Service. Should be called when the Activity is started.
     */
    public void connect() {
        mediaBrowser.connect();
    }

    /**
     * Disconnect from the Service. Should be called when the Activity is stopped.
     */
    public void disconnect() {
        mediaBrowser.disconnect();
    }

    /**
     * Start playing a song on the Service. Won't do anything if the Service has not been bound yet.
     *
     * @param id String id for the Song to play.
     */
    public void play(String id) {
        if (mediaController == null)
            return;

        mediaController.getTransportControls().playFromMediaId(id, null);
    }

    /**
     * Play a Song object instead of from an id.
     *
     * @param song Song to play.
     */
    public void play(Song song) {
        play(song.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    }

    /**
     * Toggle the player between playing or paused. Won't do anything if the Service has not been
     * bound yet. Either calls pause or play, so this should only be called when a Song actually is
     * already playing.
     */
    public void toggle() {
        if (mediaController == null)
            return;

        int playbackState = mediaController.getPlaybackState().getState();
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            mediaController.getTransportControls().pause();
        else
            mediaController.getTransportControls().play();
    }

}
