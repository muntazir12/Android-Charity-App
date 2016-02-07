package com.hjejni;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hjejni.helper.SQLiteHandler;
import com.hjejni.helper.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by muntazir on 13/10/15.
 */



public class RMessageView extends ActionBarActivity {

    String pid;
    TextView remail;
    TextView rpid;
    TextView semail;
    TextView sname;
    TextView rname;
    TextView message;
    TextView rtable;

    Toolbar toolbar;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();


    private SQLiteHandler db;
    private SessionManager session;

    // single product url
    private static final String url_message_detials = "http://52.89.129.22/android_login_api/api/get_message.php";
    private static final String url_delete_message = "http://52.89.129.22/android_login_api/api/delete_smessage.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCT = "message";
    private static final String TAG_PID = "pid";
    private static final String TAG_RPID = "rpid";
    private static final String TAG_REMAIL = "remail";
    private static final String TAG_SNAME = "sname";
    private static final String TAG_RNAME = "rname";
    private static final String TAG_SEMAIL = "semail";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_RTABLE = "rtable";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_rmessage);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sent_msg);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Getting complete product details in background thread
        new GetMessageDetails().execute();


        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Message Detail");

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());


        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        pid = i.getStringExtra(TAG_PID);

        Button delete = (Button) findViewById(R.id.btndelete);

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0) {
                // starting background task to delete product
                new DeleteMessage().execute();
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(RMessageView.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Background Async Task to Get complete product details
     */
    class GetMessageDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RMessageView.this);
            pDialog.setMessage("Loading request details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            /*pDialog.show();*/
        }

        /**
         * Getting product details in background thread
         */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pid", pid));

                        // getting product details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_message_detials, "GET", params);

                        // check your log for json response
                        Log.d("Single Product Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray messageObj = json
                                    .getJSONArray(TAG_PRODUCT); // JSON Array

                            // get first product object from JSON Array
                            JSONObject messages = messageObj.getJSONObject(0);


                            remail = (TextView) findViewById(R.id.remail);
                            semail = (TextView) findViewById(R.id.semail);
                            sname = (TextView) findViewById(R.id.sname);
                            rname = (TextView) findViewById(R.id.rname);
                            message = (TextView) findViewById(R.id.message);
                            rpid = (TextView) findViewById(R.id.rpid);
                            rtable = (TextView) findViewById(R.id.rtable);

                            // display product data in EditText

                            remail.setText(messages.getString(TAG_REMAIL));
                            semail.setText(messages.getString(TAG_SEMAIL));
                            sname.setText(messages.getString(TAG_SNAME));
                            rname.setText(messages.getString(TAG_RNAME));
                            message.setText(messages.getString(TAG_MESSAGE));
                            rpid.setText(messages.getString(TAG_RPID));
                            rtable.setText(messages.getString(TAG_RTABLE));

                            final String email = semail.getText().toString();
                            final String name = sname.getText().toString();
                            final String pid = rpid.getText().toString();
                            final String table = rtable.getText().toString();

                            Button message = (Button) findViewById(R.id.btnmessage);
                            message.setOnClickListener(new View.OnClickListener()

                            {

                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(RMessageView.this, MessageBtn.class);
                                    i.putExtra("email", email);
                                    i.putExtra("name", name);
                                    i.putExtra("table", table);
                                    i.putExtra("pid", pid);
                                    startActivity(i);

                                }
                            });

                            Button status = (Button) findViewById(R.id.btnstatus);

                            // SqLite database handler
                            db = new SQLiteHandler(getApplicationContext());
                            // Fetching user details from sqlite
                            HashMap<String, String> user = db.getUserDetails();
                            String type = user.get("type");

                            if (type.equals("Donor")) {
                                status.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                status.setVisibility(View.INVISIBLE);
                            }

                            status.setOnClickListener(new View.OnClickListener()

                            {

                                @Override
                                public void onClick(View v) {

                                    if (table.equals("Haj")) {
                                        Intent i = new Intent(RMessageView.this, mHajView.class);
                                        i.putExtra("pid", pid);
                                        startActivity(i);
                                    }
                                    else if (table.equals("Umrah")) {
                                        Intent i = new Intent(RMessageView.this, mUmrahView.class);
                                        i.putExtra("pid", pid);
                                        startActivity(i);
                                    }

                                }
                            });



                        } else {
                            // product with pid not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details


            pDialog.dismiss();
        }


    }

    class DeleteMessage extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RMessageView.this);
            pDialog.setMessage("Deleting Message...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting product
         */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pid", pid));

                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        url_delete_message, "POST", params);

                // check your log for json response
                Log.d("Delete Product", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // product successfully deleted
                    // notify previous activity by sending code 100
                    Intent i = getIntent();
                    // send result code 100 to notify about product deletion
                    setResult(100, i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details


            pDialog.dismiss();
        }


    }

}