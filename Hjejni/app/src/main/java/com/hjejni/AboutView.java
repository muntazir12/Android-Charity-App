package com.hjejni;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

/**
 * Created by muntazir on 14/11/15.
 */
public class AboutView extends ActionBarActivity {

    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_about);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.about);

        WebView browser = (WebView) findViewById(R.id.WebAbout);
        browser.loadUrl("http://52.89.129.22/android_login_api/about.html");
    }

}
