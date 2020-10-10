package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

/**
 * Callback class implementing the various methods for communicating between the
 * {@link PlayerService} and a client.
 *
 * @author Riccardo De Zen.
 */
public class PlayerCallback extends MediaSessionCompat.Callback {

    private PlayerService playerService;

    /**
     * Creates a Callback instance, which refers to a certain {@link PlayerService}.
     * This object expects the given Service to be already running. You probably want to create this
     * from such Service's {@link PlayerService#onCreate()} method.
     *
     * @param playerService The Service instance to bind. Must already be created.
     */
    public PlayerCallback(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * The Media session received a play command.
     * TODO : Audio Focus, noisy
     */
    @Override
    public void onPlay() {
        super.onPlay();
        // Start (or restart) the Service.
        playerService.startService(new Intent(playerService, PlayerService.class));

        // Set the Media Session as active.
        playerService.getMediaSession().setActive(true);

        // Play the song on the Service.
        playerService.play();

        // Put the Service in the foreground.
        playerService.startForeground(
                PlayerService.NOTIFICATION_ID,
                playerService.getNotification()
        );
    }

    /**
     * The Media session received a pause command.
     * TODO noisy
     */
    @Override
    public void onPause() {
        super.onPause();
        playerService.pause();
        playerService.stopForeground(false);
    }

    /**
     * The Media session received a stop command.
     */
    @Override
    public void onStop() {
        super.onStop();
        playerService.stopPlayer();
        playerService.getMediaSession().setActive(false);
        playerService.stopForeground(false);
        playerService.stopSelf();
    }

}
