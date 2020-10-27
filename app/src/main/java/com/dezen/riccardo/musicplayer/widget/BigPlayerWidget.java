package com.dezen.riccardo.musicplayer.widget;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.R;
import com.dezen.riccardo.musicplayer.song.SongManager;

public class BigPlayerWidget extends PlayerWidget {

    private SongManager songManager;
    private ImageView thumbnailView;

    public BigPlayerWidget(Context context) {
        super(context);
    }

    public BigPlayerWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BigPlayerWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BigPlayerWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Initialize the various components, same as PlayerWidget, but also reference the SongManager
     * to retrieve the Song thumbnails and an ImageView to display it.
     *
     * @param context The Context.
     */
    @Override
    protected void init(@NonNull Context context) {
        super.init(context);
        songManager = SongManager.getInstance(context);
        thumbnailView = root.findViewById(R.id.thumbnail_big);
        thumbnailView.setClipToOutline(true);
    }

    @Override
    protected int getWidgetLayout() {
        return R.layout.big_player_widget_layout;
    }

    /**
     * @param metadata The new metadata, if it is not null, show the title of the song and retrieve
     *                 the thumbnail.
     */
    @Override
    protected void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);
        if (metadata == null || metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) == null)
            return;
        String id = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        songManager.getThumbnail(id, (resultId, thumbnail) ->
                thumbnailView.setImageBitmap(thumbnail)
        );
    }
}
