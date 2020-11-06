package com.dezen.riccardo.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.song.Song;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class used to simplify MediaBrowserClient usage. Communicates with a {@link PlayerService}.
 * {@link PlayerClient#connect()} should be called when the host Activity is started.
 * {@link PlayerClient#disconnect()} should be called when the Activity is stopped.
 * The class is an Object pool, only one client is allowed for a certain Context. Context are not
 * referenced directly, their hashcode is used instead.
 *
 * @author Riccardo De Zen.
 */
public class PlayerClient extends MediaControllerCompat.Callback {

    private static final Map<Integer, PlayerClient> instancePool = new HashMap<>();

    private final Set<Observer> observers = new HashSet<>();

    private final MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    /**
     * @param context The {@link Context} hosting the media player. If it is an Activity, it will
     *                be set in the MediaController.
     */
    private PlayerClient(@NonNull Context context) {
        // Defining callbacks.
        MediaBrowserCompat.ConnectionCallback callbacks =
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        // Will be non-null when this method is called.
                        MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                        // Initialize the media controller.
                        mediaController = new MediaControllerCompat(
                                context, token
                        );
                        if (context instanceof Activity)
                            MediaControllerCompat.setMediaController((Activity) context,
                                    mediaController);

                        // Register callbacks if they have been set.
                        mediaController.registerCallback(PlayerClient.this);
                        PlayerClient.this.onMetadataChanged(mediaController.getMetadata());
                        PlayerClient.this.onPlaybackStateChanged(mediaController.getPlaybackState());
                    }

                    @Override
                    public void onConnectionFailed() {
                        super.onConnectionFailed();
                        Toast.makeText(context, R.string.connection_failed_error,
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        super.onConnectionSuspended();
                        Toast.makeText(context, R.string.service_shut_down_warning,
                                Toast.LENGTH_LONG).show();
                    }
                };

        // Initialize MediaBrowser.
        mediaBrowser = new MediaBrowserCompat(
                context,
                new ComponentName(context, PlayerService.class),
                callbacks,
                null
        );
    }

    /**
     * Retrieve the PlayerClient for a certain Context. If the Context is an Activity, the
     * MediaController in this object will be recorded as the controller for the Activity.
     *
     * @param context The calling Context.
     * @return The PlayerClient instance for the Context, if existing, or a newly created one.
     */
    public static PlayerClient of(@NonNull Context context) {
        PlayerClient instance = instancePool.get(context.hashCode());
        if (instance != null)
            return instance;
        PlayerClient newInstance = new PlayerClient(context);
        instancePool.put(context.hashCode(), newInstance);
        return newInstance;
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
     * @param observer The callback for the Media Controller.
     * @param context  Is used by the callback to retrieve the SongManager when available.
     */
    public void observe(@NonNull Observer observer, @NonNull Context context) {
        observers.add(observer);
        if (mediaController != null)
            observer.onMetadataChanged(mediaController.getMetadata());
    }

    /**
     * Remove an Observer.
     */
    public void removeObserver(@NonNull Observer observer) {
        observers.remove(observer);
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

    /**
     * Tries to skip to the next song if possible.
     */
    public void next() {
        if (mediaController == null)
            return;

        mediaController.getTransportControls().skipToNext();
    }

    /**
     * Tries to skip to the previous song if possible.
     */
    public void previous() {
        if (mediaController == null)
            return;

        mediaController.getTransportControls().skipToPrevious();
    }

    // ? Overrides for controller callback.

    @Override
    public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
        super.onAudioInfoChanged(info);
        for (Observer c : observers)
            c.onAudioInfoChanged(info);
    }

    @Override
    public void onCaptioningEnabledChanged(boolean enabled) {
        super.onCaptioningEnabledChanged(enabled);
        for (Observer c : observers)
            c.onCaptioningEnabledChanged(enabled);
    }

    @Override
    public void onExtrasChanged(Bundle extras) {
        super.onExtrasChanged(extras);
        for (Observer c : observers)
            c.onExtrasChanged(extras);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);
        for (Observer c : observers)
            c.onMetadataChanged(metadata);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        super.onPlaybackStateChanged(state);
        for (Observer c : observers)
            c.onPlaybackStateChanged(state);
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        super.onQueueChanged(queue);
        for (Observer c : observers)
            c.onQueueChanged(queue);
    }

    @Override
    public void onQueueTitleChanged(CharSequence title) {
        super.onQueueTitleChanged(title);
        for (Observer c : observers)
            c.onQueueTitleChanged(title);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        super.onRepeatModeChanged(repeatMode);
        for (Observer c : observers)
            c.onRepeatModeChanged(repeatMode);
    }

    @Override
    public void onSessionDestroyed() {
        super.onSessionDestroyed();
        for (Observer c : observers)
            c.onSessionDestroyed();
    }

    @Override
    public void onSessionEvent(String event, Bundle extras) {
        super.onSessionEvent(event, extras);
        for (Observer c : observers)
            c.onSessionEvent(event, extras);
    }

    @Override
    public void onSessionReady() {
        super.onSessionReady();
        for (Observer c : observers)
            c.onSessionReady();
    }

    @Override
    public void onShuffleModeChanged(int shuffleMode) {
        super.onShuffleModeChanged(shuffleMode);
        for (Observer c : observers)
            c.onShuffleModeChanged(shuffleMode);
    }

    // ? Overrides for control via a View.

    public void start() {
        int playbackState = mediaController.getPlaybackState().getState();
        if (playbackState != PlaybackStateCompat.STATE_PLAYING)
            mediaController.getTransportControls().play();
    }

    public void pause() {
        int playbackState = mediaController.getPlaybackState().getState();
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            mediaController.getTransportControls().pause();
    }

    public int getDuration() {
        if (mediaController == null || mediaController.getMetadata() == null)
            return -1;
        return (int) mediaController.getMetadata().getLong(
                MediaMetadataCompat.METADATA_KEY_DURATION
        );
    }

    public int getCurrentPosition() {
        if (mediaController == null || mediaController.getPlaybackState() == null)
            return -1;
        return (int) mediaController.getPlaybackState().getPosition();
    }

    public void seekTo(int pos) {
        if (mediaController != null)
            mediaController.getTransportControls().seekTo(pos);
    }

    public boolean isPlaying() {
        int playbackState = mediaController.getPlaybackState().getState();
        return playbackState == PlaybackStateCompat.STATE_PLAYING;
    }

    public boolean canPause() {
        return true;
    }

    // ? Useful getters

    @Nullable
    public MediaMetadataCompat getMetadata() {
        return (mediaController == null) ? null : mediaController.getMetadata();
    }

    @Nullable
    public PlaybackStateCompat getPlaybackState() {
        return (mediaController == null) ? null : mediaController.getPlaybackState();
    }

    public abstract static class Observer extends MediaControllerCompat.Callback {
    }
}
