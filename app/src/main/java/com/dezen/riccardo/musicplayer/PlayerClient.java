package com.dezen.riccardo.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.support.v4.media.MediaBrowserCompat;
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
    // TODO allow registering for mediaController callbacks
    //  since this is initialized asynchronously, save the listener and assign it later, also
    //  provide it the current metadata since it lost the current event.
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback controllerCallback;
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

                // Register callbacks if they have been set.
                if (controllerCallback != null) {
                    mediaController.registerCallback(controllerCallback);
                    controllerCallback.onMetadataChanged(mediaController.getMetadata());
                }
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
     * Registers callbacks for the media controller. If the mediaController has not been set yet,
     * the callbacks will be registered when it's available. When the callbacks are registered, the
     * metadata of the song currently being played will be provided.
     * Only one callback is allowed at a time.
     *
     * @param callback The callback for the Media Controller.
     */
    public void setListener(@NonNull MediaControllerCompat.Callback callback) {
        clearListener();
        controllerCallback = callback;
        if (mediaController != null) {
            mediaController.registerCallback(callback);
            callback.onMetadataChanged(mediaController.getMetadata());
        }
    }

    /**
     * Remove the event listener.
     */
    public void clearListener() {
        if (mediaController != null && controllerCallback != null)
            mediaController.unregisterCallback(controllerCallback);
        controllerCallback = null;
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
        play(song.getId());
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
