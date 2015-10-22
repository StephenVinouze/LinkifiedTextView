package com.stephenvinouze.linkifiedtextviewsample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stephenvinouze.linkifiedtextviewsample.R;

/**
 * Created by Stephen Vinouze on 22/10/15.
 */
public class FontFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.font_layout, container, false);
    }
}
