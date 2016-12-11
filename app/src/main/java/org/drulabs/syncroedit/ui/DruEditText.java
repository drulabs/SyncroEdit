package org.drulabs.syncroedit.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import org.drulabs.syncroedit.R;

/**
 * Authored by KaushalD on 6/21/2016.
 */
public class DruEditText extends EditText {
    private static final int LRU_CACHE_LIMIT = 5;
    /**
     * An <code>LruCache</code> for previously loaded typefaces.
     */
    private static LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(LRU_CACHE_LIMIT);

    public DruEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = null;
        try {

            // Get our custom attributes
            a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.CustomFont, 0, 0);
            String typefaceName = a.getString(
                    R.styleable.RobotoFont_roboFont);

            if (!isInEditMode() && !TextUtils.isEmpty(typefaceName)) {
                Typeface typeface = sTypefaceCache.get(typefaceName);

                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(),
                            String.format("fonts/%s.ttf", typefaceName));

                    // Cache the Typeface object
                    sTypefaceCache.put(typefaceName, typeface);
                }
                setTypeface(typeface);

                // Note: This flag is required for proper typeface rendering
                setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        } catch (Exception e) {
            Log.e("CustomTextView::", Log.getStackTraceString(e));
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }
}
