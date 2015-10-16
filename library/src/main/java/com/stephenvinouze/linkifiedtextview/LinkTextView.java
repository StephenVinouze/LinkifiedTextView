package com.stephenvinouze.linkifiedtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.stephenvinouze.linktextview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkTextView extends TextView {

    public static final int LINK_TYPE_NONE = 0;
    public static final int LINK_TYPE_WEB = 1;
    public static final int LINK_TYPE_HASHTAG = 1<<1;
    public static final int LINK_TYPE_SCREENNAME = 1<<2;
    public static final int LINK_TYPE_EMAIL = 1<<3;
    public static final int LINK_TYPE_PHONE = 1<<4;
    public static final int LINK_TYPE_ALL = LINK_TYPE_WEB | LINK_TYPE_HASHTAG | LINK_TYPE_SCREENNAME | LINK_TYPE_EMAIL | LINK_TYPE_PHONE;

    public interface OnLinkClickListener {
        void onLinkClick(View textView, String link, int type);
    }

    private class Hyperlink {
        int type;
        CharSequence textSpan;
        InternalURLSpan span;
        int start;
        int end;
        int color;
    }

    private Pattern hashtagPattern = Pattern.compile("(#\\w+)");
    private Pattern screenNamePattern = Pattern.compile("(@\\w+)");
    private Pattern hyperlinkPattern = Pattern.compile("([Hh][tT][tT][pP][sS]?://[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])");
    private Pattern emailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    private Pattern phonePattern = Pattern.compile("(0[1-68][-.\\s]?(\\d{2}[-.\\s]?){3}\\d{2})");

    private int mLinkType = LINK_TYPE_NONE;
    private int mLinkTextColor = Color.BLUE;
    private boolean mHitLink = false;

    private List<Hyperlink> mLinks = new ArrayList<>();
    private OnLinkClickListener mListener;

	public LinkTextView(Context context) {
		super(context);
	}

	public LinkTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

        String linkText = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkTextView);

        for (int i = 0; i < a.getIndexCount(); ++i) {

            int attr = a.getIndex(i);
            if (attr == R.styleable.LinkTextView_tvLinkText) {
                linkText = a.getString(attr);
            }
            else if (attr == R.styleable.LinkTextView_tvLinkTextColor) {
                setLinkColor(a.getColor(attr, mLinkTextColor));
            }
            else if (attr == R.styleable.LinkTextView_tvLinkType) {
                setLinkType(a.getInt(attr, LINK_TYPE_NONE));
            }
        }

        a.recycle();

        if (linkText != null) {
            setLinkText(linkText);
        }
	}

    public void setOnLinkClickListener(OnLinkClickListener listener) {
        mListener = listener;
    }

    public boolean isDetectingLinks() {
        return mLinkType != LINK_TYPE_NONE;
    }

    public void setLinkType(int type) {
        mLinkType = type;
    }

    public void setLinkColor(int color) {
        mLinkTextColor = color;
    }

    public void setLinkText(String text) {
        mLinks.clear();

        if (containsLinkType(LINK_TYPE_WEB)) {
            gatherLinks(text, hyperlinkPattern, LINK_TYPE_WEB);
        }
        if (containsLinkType(LINK_TYPE_HASHTAG)) {
            gatherLinks(text, hashtagPattern, LINK_TYPE_HASHTAG);
        }
        if (containsLinkType(LINK_TYPE_SCREENNAME)) {
            gatherLinks(text, screenNamePattern, LINK_TYPE_SCREENNAME);
        }
        if (containsLinkType(LINK_TYPE_EMAIL)) {
            gatherLinks(text, emailPattern, LINK_TYPE_EMAIL);
        }
        if (containsLinkType(LINK_TYPE_PHONE)) {
            gatherLinks(text, phonePattern, LINK_TYPE_PHONE);
        }

        SpannableString linkableText = new SpannableString(text);

        if (!mLinks.isEmpty()) {
            for (Hyperlink linkSpec : mLinks) {
                linkableText.setSpan(linkSpec.span, linkSpec.start, linkSpec.end, 0);
            }

            MovementMethod m = getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (getLinksClickable()) {
                    setMovementMethod(LocalLinkMovementMethod.getInstance());
                }
            }
        }

        setText(linkableText);
    }

    private boolean containsLinkType(int type) {
        return (mLinkType & type) == type;
    }

    private void gatherLinks(String s, Pattern pattern, int type) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            Hyperlink link = new Hyperlink();

            link.type = type;
            link.textSpan = s.subSequence(start, end);
            link.color = mLinkTextColor;
            link.span = new InternalURLSpan(link.textSpan.toString(), link.type, link.color);
            link.start = start;
            link.end = end;

            mLinks.add(link);
        }
    }

    @Override
    public boolean hasFocusable() {
        return !isDetectingLinks() && super.hasFocusable();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHitLink = false;
        boolean res = super.onTouchEvent(event);

        if (isDetectingLinks())
            return mHitLink;

        return res;
    }

    public class InternalURLSpan extends ClickableSpan {

        private String clickedSpan;
        private int clickedType;
        private int clickedColor;

        public InternalURLSpan(String span, int type, int color) {
            clickedSpan = span;
            clickedType = type;
            clickedColor = color;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.bgColor = Color.TRANSPARENT;
            ds.setColor(clickedColor);
            ds.setUnderlineText(true);
        }

        @Override
        public void onClick(View textView) {
            if (mListener != null) {
                mListener.onLinkClick(textView, clickedSpan, clickedType);
            }
        }
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;


        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    }
                    else {
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                    }

                    if (widget instanceof LinkTextView) {
                        ((LinkTextView) widget).mHitLink = true;
                    }
                    return true;
                }
                else {
                    Selection.removeSelection(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }
	
}