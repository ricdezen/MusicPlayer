package com.dezen.riccardo.musicplayer.widget;

import android.content.Context;
import android.os.Build;
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

    private static final String DEFAULT_TIME_TEXT = "00:00";

    protected View root;
    private SeekBar seekBar;
    private TextView titleView;
    private TextView positionView;
    private TextView durationView;
    private ImageButton playPauseButton;
    private PlayerClient client;

    private boolean dragging = false;
    private int barMax = 100;

    /**
     * Observer for the given PlayerClient.
     */
    private final PlayerClient.Observer observer = new PlayerClient.Observer() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            PlayerWidget.this.onPlaybackStateChanged(state);
        }


        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            PlayerWidget.this.onMetadataChanged(metadata);
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
    protected void init(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(getWidgetLayout(), this, true);

        // Title, needs to be selected for marquee to work.
        titleView = root.findViewById(R.id.widget_song_title);
        titleView.setSelected(true);

        // Widgets for duration and position.
        positionView = root.findViewById(R.id.song_position);
        durationView = root.findViewById(R.id.song_duration);

        // Play/pause button.
        playPauseButton = root.findViewById(R.id.central_button);
        playPauseButton.setOnClickListener((v) -> {
            if (client != null)
                client.toggle();
        });
        View nextButton = root.findViewById(R.id.next_button);
        nextButton.setOnClickListener((v) -> {
            if (client != null)
                client.next();
        });
        View previousButton = root.findViewById(R.id.previous_button);
        previousButton.setOnClickListener((v) -> {
            if (client != null)
                client.previous();
        });

        // Song progress bar.
        seekBar = root.findViewById(R.id.song_progress);
        seekBar.setMax(barMax);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Ignored.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            /**
             * Warn that the user is dragging the bar, so it should not be updated every second.
             *
             * @param seekBar The bar being touched.
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dragging = false;
                client.seekTo(seekBar.getProgress());
            }
        });

        // Hide by default.
        hide();
    }

    /**
     * @param newController The controller for the Widget, pass null to remove the controller and
     *                      stop updating the view, hiding it.
     * @param context       The Context.
     */
    public void setController(@Nullable PlayerClient newController, @NonNull Context context) {
        if (this.client != null)
            this.client.removeObserver(observer);
        if (newController != null)
            newController.observe(observer, context);
        this.client = newController;
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
     * Disable the seek bar and display a default title.
     */
    public void disable() {
        seekBar.setEnabled(false);
        seekBar.setProgress(barMax / 2);
        titleView.setText(getResources().getString(R.string.no_metadata_error));
        positionView.setText(DEFAULT_TIME_TEXT);
        durationView.setText(DEFAULT_TIME_TEXT);
        positionView.setEnabled(false);
        durationView.setEnabled(false);
    }

    /**
     * Re-enable the seek bar.
     */
    public void enable() {
        seekBar.setEnabled(true);
        positionView.setEnabled(true);
        durationView.setEnabled(true);
    }

    /**
     * Interrogate the controller on the playback position and update the progress bar.
     * This method will also post a copy of itself if the controller is not null and the user is
     * not dragging the seekBar.
     */
    public void updateProgress() {
        if (client != null)
            Utils.onMainThread(this::updateProgress, 1000);
        if (client == null || dragging)
            return;

        int duration = client.getDuration();
        int position = client.getCurrentPosition();

        if (duration >= 0 && position >= 0) {
            positionView.setText(Utils.millisToString(position));
            durationView.setText(Utils.millisToString(duration));
            int progress = (int) Math.round((1.0 * position) / duration * barMax);
            if (Build.VERSION.SDK_INT < 24)
                seekBar.setProgress(progress);
            else
                seekBar.setProgress(progress, true);
        }
    }

    /**
     * Method to retrieve the layout for the Widget. Must have all the required views.
     *
     * @return {@link R.layout#player_widget_layout}.
     */
    protected int getWidgetLayout() {
        return R.layout.player_widget_layout;
    }

    /**
     * @param state The new state, show the widget only if the state is non null and is
     *              playing or paused.
     */
    protected void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (state == null ||
                (state.getState() != PlaybackStateCompat.STATE_PLAYING &&
                        state.getState() != PlaybackStateCompat.STATE_PAUSED))
            hide();
        else {
            show();
            playPauseButton.setImageResource(Utils.getButtonIcon(state.getState()));
        }
    }

    /**
     * @param metadata The new metadata, if it is not null, show the title of the song.
     */
    protected void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            disable();
            return;
        }
        titleView.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        int newDuration = client.getDuration();
        // Should never happen, you never know.
        if (newDuration < 0) {
            disable();
            return;
        }
        enable();
        barMax = newDuration;
        seekBar.setMax(barMax);
    }
}
