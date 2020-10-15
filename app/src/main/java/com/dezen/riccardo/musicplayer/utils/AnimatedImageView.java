package com.dezen.riccardo.musicplayer.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

/**
 * Temporary solution for an ImageView that always plays its Drawable's animation in loop when
 * drawn.
 * Its Drawable MUST be an AnimatedVectorDrawable or AnimatedVectorDrawableCompat.
 */
public class AnimatedImageView extends androidx.appcompat.widget.AppCompatImageView {

    private static final String VECTOR_ERROR = "Only AnimatedVectorDrawable (or compat) supported.";

    private boolean animating = false;

    public AnimatedImageView(Context context) {
        super(context);
    }

    public AnimatedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param drawable New Drawable for this image.
     * @throws IllegalArgumentException Rejects all drawables that are not AnimatedVectorDrawable or
     *                                  AnimatedVectorDrawableCompat.
     */
    @Override
    public void setImageDrawable(@Nullable Drawable drawable) throws IllegalArgumentException {
        if (!(drawable instanceof AnimatedVectorDrawableCompat) && !(drawable instanceof AnimatedVectorDrawable))
            throw new IllegalArgumentException(VECTOR_ERROR);

        super.setImageDrawable(drawable);

        animating = false;
        Animatable animated = (Animatable) drawable;
        // The animation loops.
        AnimatedVectorDrawableCompat.registerAnimationCallback(drawable,
                new Animatable2Compat.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        super.onAnimationEnd(drawable);
                        animated.start();
                    }
                });
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.animating = false;
    }

    /**
     * When drawing the image view, start it's animation.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable d = this.getDrawable();
        if (this.animating)
            return;
        Animatable image = (Animatable) d;
        image.start();
        animating = true;
    }
}
