package com.dezen.riccardo.musicplayer.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.R;

public class BigPlayerWidget extends PlayerWidget {

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

    @Override
    protected int getWidgetLayout() {
        return R.layout.big_player_widget_layout;
    }
}
