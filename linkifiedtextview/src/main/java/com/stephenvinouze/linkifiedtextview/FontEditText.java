package com.stephenvinouze.linkifiedtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by Stephen Vinouze on 16/10/15.
 */
public class FontEditText extends AppCompatEditText {

    public FontEditText(Context context) {
        super(context);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);

        for (int i = 0; i < a.getIndexCount(); ++i) {

            int attr = a.getIndex(i);
            if (attr == R.styleable.FontTextView_tvFontName) {
                setFontName(a.getString(attr));
            }
        }

        a.recycle();
    }

    public void setFontName(String fontname) {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontname));
    }

}
