package com.dezen.riccardo.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

import java.io.IOException;
import java.util.List;

/**
 * Service that manages the playback of songs.
 *
 * @author Riccardo De Zen.
 */
public class PlayerService extends Service {

    private static final int NOTIFICATION_ID = 1234;

    private SongManager songManager;
    private MediaPlayer mediaPlayer;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.getInstance(this).createChannelIfNeeded();
    }

    /**
     * When the Service is started the useful Objects are initialized.
     *
     * @param intent  The Intent that started the Service.
     * @param flags   The flags for the Intent.
     * @param startId The startId for this method.
     * @return The value of the method from the superclass.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (songManager == null)
            songManager = SongManager.getInstance(this);
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * @param intent The Intent with the bind request to this Service.
     * @return A {@link MusicController} binder, containing the actions that can be performed on
     * the Service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicController(this);
    }

    /**
     * Method to start playing a song at a certain position in the list.
     *
     * @param position The position of the song.
     */
    public void play(int position) {
        List<Song> songs = songManager.getSongs().getValue();
        if (songs == null) return;
        if (position < 0 || position >= songs.size()) return;

        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(songs.get(position).getDataPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            startForeground(
                    NOTIFICATION_ID,
                    NotificationHelper.getInstance(this).getServiceNotification(this)
            );
        } catch (IOException e) {
            Toast.makeText(this, "Impossibile riprodurre il file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to pause the playback of a song.
     */
    public void pause() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    /**
     * Method to resume the playback of a song.
     */
    public void resume() {
        if (mediaPlayer != null)
            mediaPlayer.start();
    }

    /**
     * When the Service is destroyed the media player is released.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
