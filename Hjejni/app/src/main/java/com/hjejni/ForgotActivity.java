package com.hjejni;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

/**
 * Created by muntazir on 21/10/15.
 */
public class ForgotActivity extends ActionBarActivity {

    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.forgot_pwd);

        WebView browser = (WebView) findViewById(R.id.webview);
        browser.loadUrl("http://52.89.129.22/android_login_api/forgot.php");
    }
}
