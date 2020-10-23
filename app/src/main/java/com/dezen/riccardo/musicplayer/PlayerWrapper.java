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
import java.util.Objects;

public class PlayerWrapper extends MediaSessionCompat.Callback {

    public static final long[] SUPPORTED_ACTIONS = new long[]{
            PlaybackStateCompat.ACTION_PLAY,
            PlaybackStateCompat.ACTION_PAUSE,
            PlaybackStateCompat.ACTION_PLAY_PAUSE,
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS,
            PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE,
            PlaybackStateCompat.ACTION_SET_REPEAT_MODE
    };

    // Current Song.
    private String currentSongId;
    // Current PlayList.
    private PlayList currentPlayList;

    // TODO store in shared preferences and init with a default.
    private int repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
    private int shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;

    // SongManager.
    private SongManager songManager;
    // The Service to manage.
    private PlayerService service;
    // MediaSession of the Service.
    private MediaSessionCompat session;
    // Notification Helper.
    private final NotificationHelper notificationHelper;
    // The media player.
    private final MediaPlayer mediaPlayer;
    // PlaybackStateBuilder.
    private final PlaybackStateCompat.Builder playbackStateBuilder;

    // Update this Player's playlist when the SongManager is updated.
    private SongManager.PlayListObserver playListObserver = (newPL) -> {
        synchronized (PlayerWrapper.this) {
            currentPlayList = newPL;
        }
    };

    // No repeat -> Just skip and stop if it is the last song. NO SHUFFLE.
    private final MediaPlayer.OnCompletionListener noRepeatListener = (mp) -> onSkipNoLoop();
    // Repeat all -> Just skip. SHUFFLE COMPATIBLE.
    private final MediaPlayer.OnCompletionListener repeatAllListener = (mp) -> onSkipToNext();
    // Repeat one -> Play the same media again. NO SHUFFLE.
    private final MediaPlayer.OnCompletionListener repeatOneListener = (mp) -> onPlayFromMediaId(currentSongId, null);

    /**
     * Construct a Player for a certain Service, it retrieves the contents of the SongManager for
     * the Service's MediaSession.
     *
     * @param service The Service hosting the Player. Used as context. Must have a non-null MediaSession token.
     */
    public PlayerWrapper(@NonNull PlayerService service) {
        // Reference SongManager for this Session.
        // The full library of Songs.
        songManager = SongManager.of(
                Objects.requireNonNull(service.getSessionToken()),
                service
        );
        songManager.observePlayList(playListObserver);
        this.currentPlayList = songManager.getPlayList();

        // Service and session.
        this.service = service;
        this.session = service.getMediaSession();

        // Media Player.
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(noRepeatListener);

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
     * TODO : Audio Focus (api 26), become noisy (broadcast receiver).
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
     * Skip to the next Song. If shuffle mode is active, will go to a random song.
     */
    @Override
    public synchronized void onSkipToNext() {
        super.onSkipToNext();

        if (currentSongId == null || currentPlayList.isEmpty())
            return;

        Song song = (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) ?
                currentPlayList.random(currentSongId) : currentPlayList.next(currentSongId);
        if (song == null)
            return;

        onPlayFromMediaId(song.getId(), null);
    }

    /**
     * Skip to the next song, but stop playback if this leads to the top of the playlist.
     * TODO Does it make sense to skip only in linear mode? Ignoring shuffle?
     */
    public synchronized void onSkipNoLoop() {
        if (currentSongId == null || currentPlayList.isEmpty())
            return;

        // If next song == first, stop, otherwise, play it.
        Song next = currentPlayList.next(currentSongId);
        if (next == null || next.getId().equals(currentPlayList.get(0).getId()))
            stop();
        else
            onPlayFromMediaId(next.getId(), null);
    }

    /**
     * TODO save history of songs to go back during shuffle.
     * Skip to the previous Song.
     */
    @Override
    public synchronized void onSkipToPrevious() {
        super.onSkipToPrevious();

        if (currentSongId == null || currentPlayList.isEmpty())
            return;

        Song song = currentPlayList.previous(currentSongId);
        if (song == null)
            return;

        onPlayFromMediaId(song.getId(), null);
    }

    /**
     * TODO other modes. Should I keep on setting shuffle to NONE?
     *
     * @param repeatMode The repeat mode. Currently manages only NONE, ONE and ALL.
     */
    @Override
    public void onSetRepeatMode(int repeatMode) {
        super.onSetRepeatMode(repeatMode);
        this.shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
        this.repeatMode = repeatMode;
        switch (repeatMode) {
            case PlaybackStateCompat.REPEAT_MODE_ONE:
                mediaPlayer.setOnCompletionListener(repeatOneListener);
                break;
            case PlaybackStateCompat.REPEAT_MODE_ALL:
                mediaPlayer.setOnCompletionListener(repeatAllListener);
                break;
            default:
                mediaPlayer.setOnCompletionListener(noRepeatListener);
        }
    }

    /**
     * TODO other modes. Should I keep setting repeat to NONE?
     *
     * @param shuffleMode The shuffle mode. Currently manages only NONE and ALL.
     */
    @Override
    public void onSetShuffleMode(int shuffleMode) {
        super.onSetShuffleMode(shuffleMode);
        this.repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
        this.shuffleMode = shuffleMode;
    }

    /**
     * Release the resources associated with this player, the Player cannot be used anymore.
     * Dereferences most of the referenced Objects.
     */
    public synchronized void release() {
        stop();
        songManager.removeObserver(playListObserver);
        mediaPlayer.release();
        // Dereference for garbage collection. Better safe than sorry.
        songManager = null;
        session = null;
        service = null;
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
                1
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
                1
        ).build());
    }

    /**
     * Method to stop the playback of a song.
     */
    public synchronized void stop() {
        mediaPlayer.stop();
        session.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer.getCurrentPosition(),
                1
        ).build());
    }

    /**
     * Set Repeat and shuffle up so that when the last song is played, the playback stops.
     */
    public void noRepeat() {
        onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
    }

    /**
     * Set Repeat and shuffle so that when a song ends, it is played again.
     */
    public void repeatOne() {
        onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
    }

    /**
     * Set Repeat and Shuffle so that when the last song is played, it starts over from the first.
     */
    public void repeatAll() {
        onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
    }

    /**
     * Set Repeat and Shuffle so that random songs are played.
     */
    public void shuffle() {
        onSetShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
    }

}
