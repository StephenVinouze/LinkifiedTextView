package com.stephenvinouze.linkifiedtextviewsample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.stephenvinouze.linkifiedtextviewsample.R;
import com.stephenvinouze.linkifiedtextviewsample.fragments.FontFragment;
import com.stephenvinouze.linkifiedtextviewsample.fragments.LinkFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        displayLinkFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.link_action:
                displayLinkFragment();
                break;

            case R.id.font_action:
                displayFontFragment();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayLinkFragment() {
        setTitle(getString(R.string.link_name));
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new LinkFragment()).commit();
    }

    private void displayFontFragment() {
        setTitle(getString(R.string.font_name));
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new FontFragment()).commit();
    }

}
