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
import android.widget.Toast;

import com.hjejni.helper.SQLiteHandler;
import com.hjejni.helper.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by muntazir on 13/10/15.
 */
public class MessageBtn extends ActionBarActivity {

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    Toolbar toolbar;

    private static String url_create_message = "http://52.89.129.22/android_login_api/api/create_message.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private SQLiteHandler db;
    private SessionManager session;

    EditText inputremail;
    EditText inputpid;
    EditText inputsemail;
    EditText inputsname;
    EditText inputmessage;
    EditText inputrname;
    EditText inputtable;
    Button btnmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btn_message);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.send_msg);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        //Fetching String from previous activity
        Intent bundle = getIntent();
        String putremail = bundle.getStringExtra("email");
        String putname = bundle.getStringExtra("name");
        String putpid = bundle.getStringExtra("pid");
        String puttable = bundle.getStringExtra("table");

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String email = user.get("email");
        String name = user.get("name");

        inputpid = (EditText) findViewById(R.id.pid);
        inputremail = (EditText) findViewById(R.id.remail);
        inputsemail = (EditText) findViewById(R.id.semail);
        inputsname = (EditText) findViewById(R.id.sname);
        inputrname = (EditText) findViewById(R.id.rname);
        inputmessage = (EditText) findViewById(R.id.message);
        inputtable = (EditText) findViewById(R.id.table);
        inputsname.setText(name);
        inputsemail.setText(email);
        inputremail.setText(putremail);
        inputrname.setText(putname);
        inputpid.setText(putpid);
        inputtable.setText(puttable);

        // Submit button
        btnmessage = (Button) findViewById(R.id.btnmessage);

        // button click event
        btnmessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String message = inputmessage.getText().toString();

                if (!message.isEmpty()) {
                    // creating new product in background thread
                    new CreateNewMessage().execute();
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Please write message", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    class CreateNewMessage extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MessageBtn.this);
            pDialog.setMessage("Sending Message..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            /*pDialog.show();*/
        }

        /**
         * Creating product
         */

        @SuppressWarnings("ResourceType")
        protected String doInBackground(String... args) {

            String rpid = inputpid.getText().toString();
            String remail = inputremail.getText().toString();
            String semail = inputsemail.getText().toString();
            String sname = inputsname.getText().toString();
            String rname = inputrname.getText().toString();
            String message = inputmessage.getText().toString();
            String rtable = inputtable.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("rpid", rpid));
            params.add(new BasicNameValuePair("remail", remail));
            params.add(new BasicNameValuePair("sname", sname));
            params.add(new BasicNameValuePair("rname", rname));
            params.add(new BasicNameValuePair("semail", semail));
            params.add(new BasicNameValuePair("message", message));
            params.add(new BasicNameValuePair("rtable", rtable));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_message,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());


            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
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
            // dismiss the dialog once done
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(),
                    "Message Sent Successfully!", Toast.LENGTH_LONG)
                    .show();
        }

    }
}