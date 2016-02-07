package com.hjejni;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.os.StrictMode;

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
 * Created by muntazir on 09/10/15.
 */
public class HajView extends ActionBarActivity {

    TextView inputname;
    TextView inputstatus;
    EditText inputremail;
    TextView inputpid;
    TextView age;
    TextView gender;
    TextView country;
    TextView family;
    TextView dependent;
    TextView previous;
    Toolbar toolbar;
    String pid;
    String rpid;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();


    private SQLiteHandler db;
    private SessionManager session;

    // single product url
    private static final String url_haj_detials = "http://52.89.129.22/android_login_api/api/get_haj.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_PRODUCT = "haj";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    private static final String TAG_AGE = "age";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_FAMILY = "family";
    private static final String TAG_DEPENDENT = "dependent";
    private static final String TAG_STATUS = "status";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_haj);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Getting complete product details in background thread
        new GetHajDetails().execute();



        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.req_detail);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());


        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        pid = i.getStringExtra(TAG_PID);



    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(HajView.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }





    /**
     * Background Async Task to Get complete product details
     */
    public class GetHajDetails extends AsyncTask<String, String, String> {

        /***
         * Before starting background thread Show Progress Dialog
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HajView.this);
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
                                url_haj_detials, "GET", params);

                        // check your log for json response
                        Log.d("Single Product Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray hajObj = json
                                    .getJSONArray(TAG_PRODUCT); // JSON Array

                            // get first product object from JSON Array
                            JSONObject haj = hajObj.getJSONObject(0);

                            // product with this pid found
                            // Edit Text
                            inputpid = (TextView) findViewById(R.id.pid);
                            inputname = (TextView) findViewById(R.id.name);
                            inputremail = (EditText) findViewById(R.id.remail);
                            age = (TextView) findViewById(R.id.age);
                            gender = (TextView) findViewById(R.id.gender);
                            family = (TextView) findViewById(R.id.family);
                            country = (TextView) findViewById(R.id.country);
                            dependent = (TextView) findViewById(R.id.dependent);
                            inputstatus = (TextView) findViewById(R.id.status);

                            // display product data in EditText

                            inputpid.setText(haj.getString(TAG_PID));
                            inputname.setText(haj.getString(TAG_NAME));
                            inputremail.setText(haj.getString(TAG_EMAIL));
                            age.setText(haj.getString(TAG_AGE));
                            gender.setText(haj.getString(TAG_GENDER));
                            family.setText(haj.getString(TAG_FAMILY));
                            country.setText(haj.getString(TAG_COUNTRY));
                            dependent.setText(haj.getString(TAG_DEPENDENT));
                            inputstatus.setText(haj.getString(TAG_STATUS));

                            final String email = inputremail.getText().toString();
                            final String name = inputname.getText().toString();
                            final String pid = inputpid.getText().toString();
                            final String table = "Haj";


                            Button message = (Button) findViewById(R.id.btnmessage);
                            message.setOnClickListener(new View.OnClickListener()

                            {

                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(HajView.this, MessageBtn.class);
                                    i.putExtra("email", email);
                                    i.putExtra("name", name);
                                    i.putExtra("pid", pid);
                                    i.putExtra("table", table);
                                    startActivity(i);


                                }
                            });

                            Button Request = (Button) findViewById(R.id.request);
                            Request.setOnClickListener(new View.OnClickListener()

                            {

                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(HajView.this, HajPrevious.class);
                                    i.putExtra("email", email);
                                    i.putExtra("pid", pid);
                                    startActivity(i);


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
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details


            pDialog.dismiss();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

}