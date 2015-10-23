package com.stephenvinouze.linkifiedtextviewsample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.stephenvinouze.linkifiedtextview.LinkTextView;
import com.stephenvinouze.linkifiedtextviewsample.R;

/**
 * Created by Stephen Vinouze on 22/10/15.
 */
public class LinkFragment extends Fragment implements LinkTextView.OnLinkClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.link_layout, container, false);

        LinkTextView webLink = (LinkTextView)layout.findViewById(R.id.web_link);
        LinkTextView hastagLink = (LinkTextView)layout.findViewById(R.id.hashtags_link);
        LinkTextView combinedLink = (LinkTextView)layout.findViewById(R.id.combined_link);
        LinkTextView allLink = (LinkTextView)layout.findViewById(R.id.all_link);

        webLink.setOnLinkClickListener(this);
        hastagLink.setOnLinkClickListener(this);
        combinedLink.setOnLinkClickListener(this);
        allLink.setOnLinkClickListener(this);

        return layout;
    }

    @Override
    public void onLinkClick(View textView, String link, int type) {
        Toast.makeText(getActivity(), link + " link was clicked", Toast.LENGTH_SHORT).show();
    }
}
