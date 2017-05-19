package com.stephenvinouze.linkifiedtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

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
    public static final int LINK_TYPE_ALL = LINK_TYPE_WEB | LINK_TYPE_HASHTAG | LINK_TYPE_SCREENNAME | LINK_TYPE_EMAIL;

    public interface OnLinkClickListener {
        void onLinkClick(View textView, String link, int type);
    }

    private class Hyperlink {
        int type;
        CharSequence textSpan;
        LinkSpan span;
        int start;
        int end;
        int color;
        boolean underline;
    }

    private Pattern hyperlinkPattern = Patterns.WEB_URL;
    private Pattern emailPattern = Patterns.EMAIL_ADDRESS;
    private Pattern hashtagPattern = Pattern.compile("(#\\w+)");
    private Pattern screenNamePattern = Pattern.compile("(@\\w+)");

    private int linkType = LINK_TYPE_NONE;
    private int linkTextColor = Color.BLUE;
    private boolean linkUnderline = false;
    private boolean hitLink = false;

    private List<Hyperlink> links = new ArrayList<>();
    private OnLinkClickListener listener;

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
                setLinkColor(a.getColor(attr, linkTextColor));
            }
            else if (attr == R.styleable.LinkTextView_tvLinkTextUnderline) {
                setLinkUnderline(a.getBoolean(attr, false));
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
        this.listener = listener;
    }

    public boolean isDetectingLinks() {
        return linkType != LINK_TYPE_NONE;
    }

    public void setLinkType(int type) {
        linkType = type;
    }

    public void setLinkColor(int color) {
        linkTextColor = color;
    }

    public void setLinkUnderline(boolean underline) {
        linkUnderline = underline;
    }

    public void setLinkText(String text) {
        links.clear();

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

        SpannableString linkableText = new SpannableString(text);

        if (!links.isEmpty()) {
            for (Hyperlink linkSpec : links) {
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
        return (linkType & type) == type;
    }

    private void gatherLinks(String s, Pattern pattern, int type) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            Hyperlink link = new Hyperlink();

            link.type = type;
            link.textSpan = s.subSequence(start, end);
            link.color = linkTextColor;
            link.underline = linkUnderline;
            link.span = new LinkSpan(link.textSpan.toString(), link.type, link.color, link.underline);
            link.start = start;
            link.end = end;

            links.add(link);
        }
    }

    @Override
    public boolean hasFocusable() {
        return !isDetectingLinks() && super.hasFocusable();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hitLink = false;
        boolean res = super.onTouchEvent(event);

        if (isDetectingLinks())
            return hitLink;

        return res;
    }

    private class LinkSpan extends ClickableSpan {

        private String mLinkText;
        private int mType;
        private int mColor;
        private boolean mUnderline;

        public LinkSpan(String linkText, int type, int color, boolean underline) {
            mLinkText = linkText;
            mType = type;
            mColor = color;
            mUnderline = underline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.bgColor = Color.TRANSPARENT;
            ds.setColor(mColor);
            ds.setUnderlineText(mUnderline);
        }

        @Override
        public void onClick(View textView) {
            if (listener != null) {
                listener.onLinkClick(textView, mLinkText, mType);
            }
        }
    }

    private static class LocalLinkMovementMethod extends LinkMovementMethod {
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
                        link[link.length - 1].onClick(widget);
                    }

                    if (widget instanceof LinkTextView) {
                        ((LinkTextView) widget).hitLink = true;
                    }
                    return true;
                }
                else {
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }
	
}