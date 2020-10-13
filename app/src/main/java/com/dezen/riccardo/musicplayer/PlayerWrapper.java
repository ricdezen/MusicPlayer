package com.dezen.riccardo.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

public class PlayerWrapper extends MediaSessionCompat.Callback {

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

    // TODO change SongManager to SongLibrary.
    public PlayerWrapper(SongManager library, PlayerService service) {
        this.library = library;
        this.service = service;
        this.session = service.getMediaSession();
        this.notificationHelper = NotificationHelper.getInstance(service);
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
        service.play(song);
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

        // Play the song on the Service.
        service.resume();

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
        service.pause();
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
        service.stopPlayer();
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

}
