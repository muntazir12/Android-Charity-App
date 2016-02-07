package com.hjejni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by muntazir on 16/10/15.
 */
public class FirstLaunch extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.launch_first);

        Button Login = (Button) findViewById(R.id.login);
        Login.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(FirstLaunch.this, LoginActivity.class);
                startActivity(i);


            }
        });

        Button Skip = (Button) findViewById(R.id.skip);
        Skip.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(FirstLaunch.this, MainActivity.class);
                startActivity(i);


            }
        });



    }
}
