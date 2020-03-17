package com.dezen.riccardo.musicplayer;

import android.os.Binder;

/**
 * Binder class defining the actions that can be performed on the Service instance.
 *
 * @author Riccardo De Zen.
 */
public class MusicController extends Binder {

    private PlayerService attachedService;

    /**
     * @param attachedService The Service that created and returned this Binder.
     */
    public MusicController(PlayerService attachedService) {
        this.attachedService = attachedService;
    }

    /**
     * Method to play a song on the Service.
     *
     * @param position The position of the song to play in the song list, must be between 0
     *                 and the size - 1.
     */
    public void play(int position) {
        attachedService.play(position);
    }

    /**
     * Method to pause the playback of the current song.
     */
    public void pause() {
        attachedService.pause();
    }

    /**
     * Method to resume the playback of the current song.
     */
    public void resume() {
        attachedService.resume();
    }

}
