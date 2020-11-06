package com.dezen.riccardo.musicplayer;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dezen.riccardo.musicplayer.widget.BigPlayerWidget;

// TODO make a widget that spans the whole screen.
public class SongActivity extends AppCompatActivity {

    private BigPlayerWidget bigPlayerWidget;
    private PlayerClient playerClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        // Get a PlayerClient.
        playerClient = PlayerClient.of(this);
        playerClient.connect();

        bigPlayerWidget = findViewById(R.id.big_player);
        bigPlayerWidget.setController(playerClient, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerClient.disconnect();
    }
}
