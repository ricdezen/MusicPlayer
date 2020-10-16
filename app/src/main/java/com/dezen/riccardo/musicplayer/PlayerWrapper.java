package com.dezen.riccardo.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dezen.riccardo.musicplayer.song.PlayList;
import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;
import com.dezen.riccardo.musicplayer.utils.NotificationHelper;
import com.dezen.riccardo.musicplayer.utils.Utils;

import java.io.IOException;

public class PlayerWrapper extends MediaSessionCompat.Callback {

    public static final long[] SUPPORTED_ACTIONS = new long[]{
            PlaybackStateCompat.ACTION_PLAY_PAUSE,
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    };

    // Current Song.
    private String currentSongId;
    // Current PlayList.
    private PlayList currentPlayList;

    // SongManager.
    private final SongManager songManager;
    // The Service to manage.
    private final PlayerService service;
    // MediaSession of the Service.
    private final MediaSessionCompat session;
    // Notification Helper.
    private final NotificationHelper notificationHelper;
    // The media player.
    private final MediaPlayer mediaPlayer;
    // PlaybackStateBuilder.
    private final PlaybackStateCompat.Builder playbackStateBuilder;

    private SongManager.PlayListObserver playListObserver = (newPL) -> {
        synchronized (PlayerWrapper.this) {
            currentPlayList = newPL;
        }
    };

    /**
     * Construct a Player for a certain Service, it retrieves the contents of the SongManager for
     * the Service's MediaSession.
     *
     * @param service The Service hosting the Player. Used as context.
     */
    public PlayerWrapper(@NonNull PlayerService service) {
        // Reference SongManager for this Session.
        // The full library of Songs.
        songManager = SongManager.of(service.getSessionToken(), service);
        songManager.observePlayList(playListObserver);
        this.currentPlayList = songManager.getPlayList();

        // Service and session.
        this.service = service;
        this.session = service.getMediaSession();

        // Media Player.
        this.mediaPlayer = new MediaPlayer();
        // TODO By default, skip to next on completion.
        this.mediaPlayer.setOnCompletionListener((mp) -> onSkipToNext());

        // Notification Utils.
        this.notificationHelper = NotificationHelper.getInstance(service);

        // Setup PlaybackStateBuilder.
        this.playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
                .setActions(Utils.bitOR(SUPPORTED_ACTIONS));
        // Default state.
        this.session.setPlaybackState(playbackStateBuilder.build());
    }

    /**
     * Play from a specific media id. If the id is not found in the SongManager class, the
     * method returns.
     *
     * @param mediaId Id, String containing the index of the item in the list.
     * @param extras  Ignored.
     */
    @Override
    public synchronized void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);

        // Return if song is not found.
        Song song = currentPlayList.get(mediaId);
        if (song == null)
            return;

        // Start (or restart) the Service.
        service.startService(new Intent(service, PlayerService.class));

        // Set the Media Session as active.
        session.setActive(true);

        // Return if the song cannot be played.
        if (!play(song))
            return;

        currentSongId = mediaId;

        // Put the Service in the foreground.
        service.startForeground(
                PlayerService.NOTIFICATION_ID,
                service.getNotification()
        );

    }

    /**
     * The Media session received a play command.
     * TODO : Audio Focus, noisy
     */
    @Override
    public synchronized void onPlay() {
        super.onPlay();

        // Start (or restart) the Service.
        service.startService(new Intent(service, PlayerService.class));

        // Set the Media Session as active.
        session.setActive(true);

        // Play the song.
        resume();

        // Put the Service in the foreground.
        service.startForeground(
                PlayerService.NOTIFICATION_ID,
                service.getNotification()
        );
    }

    /**
     * The Media session received a pause command.
     * TODO noisy
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
        if (!mediaPlayer.isPlaying())
            return;
        pause();
        // Stop being in the foreground.
        service.stopForeground(false);
        // Update the notification.
        notificationHelper.notify(PlayerService.NOTIFICATION_ID, service.getNotification());
    }

    /**
     * The Media session received a stop command.
     */
    @Override
    public synchronized void onStop() {
        super.onStop();
        stop();
        session.setActive(false);
        service.stopForeground(true);
        service.stopSelf();
    }

    /**
     * Skip to the next Song.
     */
    @Override
    public synchronized void onSkipToNext() {
        super.onSkipToNext();

        if (currentSongId == null)
            return;

        Song song = currentPlayList.next(currentSongId);
        if (song == null)
            return;

        onPlayFromMediaId(song.getId(), null);
    }

    /**
     * Skip to the previous Song.
     */
    @Override
    public synchronized void onSkipToPrevious() {
        super.onSkipToPrevious();

        if (currentSongId == null)
            return;

        Song song = currentPlayList.previous(currentSongId);
        if (song == null)
            return;

        onPlayFromMediaId(song.getId(), null);
    }

    /**
     * Custom actions. Actions allowed:
     * - "SET_PLAYLIST": Provide a playlist's name in the extras. The Playlist must be available
     * in {@link }. If not, nothing will be done.
     * TODO add class name.
     *
     * @param action Action String.
     * @param extras Extras for the Action.
     */
    @Override
    public void onCustomAction(String action, Bundle extras) {
        super.onCustomAction(action, extras);
    }

    /**
     * TODO Stop observing, somehow release SongManager.
     * Release the resources associated with this player, the Player cannot be used anymore.
     */
    public synchronized void release() {
        stop();
        songManager.removeObserver(playListObserver);
        mediaPlayer.release();
    }

    /**
     * Prepare new song and play.
     *
     * @param song The song to play.
     * @return True if the song was prepared and played successfully, false otherwise.
     */
    private synchronized boolean play(Song song) {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();

        if (!prepare(song))
            return false;

        resume();
        return true;
    }

    /**
     * Method to prepare a song to play. Resets the current mediaPlayer.
     * If an error occurs, a Toast is displayed.
     *
     * @param song The song for which to prepare playback.
     * @return Returns true if the media player was prepared successfully, false if an error
     * occurred and the media cannot be played.
     */
    public synchronized boolean prepare(Song song) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(service, song.getUri());
            mediaPlayer.prepare();
            session.setMetadata(song.getMetadata());
            return true;
        } catch (RuntimeException | IOException e) {
            Toast.makeText(service, R.string.file_open_error, Toast.LENGTH_SHORT).show();
            session.setMetadata(null);
            return false;
        }
    }

    /**
     * Method to pause the playback of a song.
     */
    public synchronized void pause() {
        mediaPlayer.pause();
        session.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

    /**
     * Resume playback without changing the song.
     */
    public synchronized void resume() {
        mediaPlayer.start();
        session.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

    /**
     * Method to stop the playback of a song.
     * TODO this kinda screws up further playback.
     */
    public synchronized void stop() {
        mediaPlayer.stop();
        session.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

}
