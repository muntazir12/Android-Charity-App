package com.hjejni;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by muntazir on 21/10/15.
 */
public class mUmrahView extends ActionBarActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    Toolbar toolbar;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    Spinner inputstatus;
    String pid;
    // url to update product
    private static final String url_update_product = "http://52.89.129.22/android_login_api/api/update_umrah.php";


    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "umrah";
    private static final String TAG_PID = "pid";
    private static final String TAG_STATUS = "status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_mumrah);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.change_sts);


        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        pid = i.getStringExtra(TAG_PID);


        Button update = (Button) findViewById(R.id.btnstatus);

        // save button click event
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                new SaveDetails().execute();
            }
        });

        inputstatus = (Spinner) findViewById(R.id.status);



    }

    class SaveDetails extends AsyncTask<String, String, String> {

        String status = inputstatus.getSelectedItem().toString();
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mUmrahView.this);
            pDialog.setMessage("Saving product ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            /*pDialog.show();*/
        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts



            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_PID, pid));
            params.add(new BasicNameValuePair(TAG_STATUS, status));

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_update_product,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product updated
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(),
                    "Request Status Changed Successfully!", Toast.LENGTH_LONG)
                    .show();
        }
    }

}