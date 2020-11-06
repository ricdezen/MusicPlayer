package com.dezen.riccardo.musicplayer.widget;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dezen.riccardo.musicplayer.R;

/**
 * This TextView always contains a SpannableString. Contains some utility methods to add or remove
 * spans corresponding to occurrences of a substring. If you set a SpannableString as the text for
 * this Widget, its spans will be reset when executing one of the methods.
 * <p>
 * XML attributes:
 * - spanColor: The color for the spans.
 */
public class QueryTextView extends androidx.appcompat.widget.AppCompatTextView {

    // TODO XML attribute
    private static final int spanColor = R.color.colorPrimary;

    public QueryTextView(@NonNull Context context) {
        super(context);
    }

    public QueryTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QueryTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Add a span corresponding to the first occurrence of the given query. Removes any other span.
     *
     * @param query The query to search for.
     */
    public void firstMatch(@NonNull String query) {
        String base = getText().toString();
        int start = base.indexOf(query);
        if (start < 0) {
            setText(base);
            return;
        }

        SpannableString spannable = new SpannableString(getText());
        spannable.setSpan(
                new ForegroundColorSpan(getResources().getColor(spanColor)),
                start,
                start + base.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        setText(spannable);
    }

    /**
     * Reset any span this widget may have.
     */
    public void reset() {
        setText(getText().toString());
    }

}
