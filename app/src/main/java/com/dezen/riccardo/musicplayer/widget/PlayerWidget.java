package com.dezen.riccardo.musicplayer.widget;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.PlayerClient;
import com.dezen.riccardo.musicplayer.R;
import com.dezen.riccardo.musicplayer.utils.Utils;

/**
 * Widget group used to display info about a song and provide a SeekBar to seek forward and backward
 * in the current Song. This Widget's visibility always defaults to GONE while no controller is
 * associated. When a controller is set then the view is shown. The progress bar updates on the main
 * Thread once every second.
 */
public class PlayerWidget extends LinearLayout {

    private View root;
    private SeekBar seekBar;
    private TextView titleView;
    private ImageButton imageButton;

    private PlayerClient controller;

    private final PlayerClient.Observer observer = new PlayerClient.Observer() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null ||
                    (state.getState() != PlaybackStateCompat.STATE_PLAYING &&
                            state.getState() != PlaybackStateCompat.STATE_PAUSED))
                hide();
            else {
                show();
                imageButton.setImageResource(Utils.getButtonIcon(state.getState()));
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null)
                return;
            titleView.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        }
    };

    public PlayerWidget(Context context) {
        super(context);
        init(context);
    }

    public PlayerWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PlayerWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * Initialize the layout.
     *
     * @param context The Context.
     */
    private void init(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(R.layout.player_widget_layout, this, true);
        titleView = root.findViewById(R.id.widget_song_title);
        titleView.setSelected(true);
        imageButton = root.findViewById(R.id.central_button);
        imageButton.setOnClickListener((v) -> {
            if (controller != null)
                controller.toggle();
        });
        seekBar = root.findViewById(R.id.song_progress);
        seekBar.setMax(1000);
        hide();
    }

    /**
     * @param newController The controller for the Widget, pass null to remove the controller and
     *                      stop updating the view, hiding it.
     * @param context       The Context.
     */
    public void setController(@Nullable PlayerClient newController, @NonNull Context context) {
        if (this.controller != null)
            this.controller.removeObserver(observer);
        if (newController != null)
            newController.observe(observer, context);
        this.controller = newController;
        observer.onMetadataChanged(newController.getMetadata());
        observer.onPlaybackStateChanged(newController.getPlaybackState());
        updateProgress();
    }

    /**
     * Show the Widget (visibility VISIBLE).
     */
    public void show() {
        setVisibility(VISIBLE);
    }

    /**
     * Hide the widget (visibility GONE).
     */
    public void hide() {
        setVisibility(GONE);
    }

    /**
     * Interrogate the controller on the playback info.
     */
    public void updateProgress() {
        if (this.controller == null)
            return;

        int duration = controller.getDuration();
        int position = controller.getCurrentPosition();

        if (duration >= 0 && position >= 0) {
            int progress = (int) Math.round((1.0 * position) / duration * 1000);
            seekBar.setProgress(progress);
        }

        Utils.onMainThread(this::updateProgress, 1000);
    }
}
