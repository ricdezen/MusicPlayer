package com.dezen.riccardo.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

import java.io.IOException;

public class PlayerWrapper extends MediaSessionCompat.Callback {

    public static final long[] SUPPORTED_ACTIONS = new long[]{
            PlaybackStateCompat.ACTION_PLAY_PAUSE,
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    };

    // The full library of Songs.
    private SongManager library;
    // The Service to manage.
    private PlayerService service;
    // MediaSession of the Service.
    private MediaSessionCompat session;
    // Notification Helper.
    private NotificationHelper notificationHelper;
    // Current Song.
    private String currentSongId;
    // The media player.
    private MediaPlayer mediaPlayer;
    // PlaybackStateBuilder.
    private PlaybackStateCompat.Builder playbackStateBuilder;

    // TODO change SongManager to SongLibrary.
    // Sets the state of the service's session, is this a good idea?
    public PlayerWrapper(SongManager library, PlayerService service) {
        this.library = library;
        this.service = service;
        this.mediaPlayer = new MediaPlayer();
        this.session = service.getMediaSession();
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
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);

        // Return if song is not found.
        Song song = library.get(mediaId);
        if (song == null)
            return;

        // Start (or restart) the Service.
        service.startService(new Intent(service, PlayerService.class));

        // Set the Media Session as active.
        session.setActive(true);

        // Play the song on the Service.
        play(song);
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
    public void onPlay() {
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
    public void onPause() {
        super.onPause();
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
    public void onStop() {
        super.onStop();
        stop();
        session.setActive(false);
        service.stopForeground(false);
        service.stopSelf();
    }

    /**
     * Skip to the next Song.
     */
    @Override
    public void onSkipToNext() {
        super.onSkipToNext();

        if (currentSongId == null)
            return;

        Song song = library.next(currentSongId);
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

        Song song = library.previous(currentSongId);
        if (song == null)
            return;

        onPlayFromMediaId(song.getId(), null);
    }

    /**
     * TODO
     * Release the resources associated with this player.
     */
    public void release() {
        stop();
        mediaPlayer.release();
    }

    /**
     * Prepare new song and play.
     *
     * @param song The song to play.
     */
    private void play(Song song) {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
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
            mediaPlayer.setDataSource(service, song.getUri());
            mediaPlayer.prepare();
            session.setMetadata(song.getMetadata());
        } catch (IOException e) {
            Toast.makeText(service, R.string.file_open_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Method to pause the playback of a song.
     */
    public void pause() {
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
    public void resume() {
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
     */
    public void stop() {
        mediaPlayer.stop();
        session.setPlaybackState(playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer.getCurrentPosition(),
                // TODO playback speed
                0
        ).build());
    }

}
