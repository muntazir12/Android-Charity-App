package com.hjejni;

/**
 * Created by muntazir on 04/10/15.
 */
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hjejni.helper.SQLiteHandler;
import com.hjejni.helper.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class UmrahBtn extends ActionBarActivity {

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    Toolbar toolbar;
    EditText inputname;
    EditText inputage;
    Spinner inputgender;
    Spinner inputcountry;
    Spinner inputfamily;
    EditText inputdependent;
    EditText inputemail;

    static final int DATE_DIALOG_ID = 0;

    // url to create new product
    private static String url_create_umrah = "http://52.89.129.22/android_login_api/api/create_umrah.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar c = Calendar.getInstance();
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH);
        int cday = c.get(Calendar.DAY_OF_MONTH);
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,  mDateSetListener,  cyear, cmonth, cday);
        }
        return null;
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        // onDateSet method
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date_selected = String.valueOf(dayOfMonth)+" /"+String.valueOf(monthOfYear+1)+" /"+String.valueOf(year);
            //Toast.makeText(getApplicationContext(), "Selected Date is ="+date_selected, Toast.LENGTH_SHORT).show();
            inputage.setText(date_selected);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btn_umrah);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.submit_req);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String email = user.get("email");
        String name = user.get("name");

        inputname = (EditText) findViewById(R.id.name);
        inputage = (EditText) findViewById(R.id.age);
        inputage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (v == inputage)
                    showDialog(DATE_DIALOG_ID);
                return false;
            }
        });
        inputdependent = (EditText) findViewById(R.id.dependent);
        inputfamily = (Spinner) findViewById(R.id.family);
        inputcountry = (Spinner) findViewById(R.id.country);
        inputgender = (Spinner) findViewById(R.id.gender);
        inputemail = (EditText) findViewById(R.id.email);
        inputemail.setText(email);
        inputname.setText(name);

        // Submit button
        Button submit = (Button) findViewById(R.id.submit);

        // button click event
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name = inputname.getText().toString();
                String age = inputage.getText().toString();

                if (!name.isEmpty() && !age.isEmpty()) {
                    // creating new product in background thread
                    new CreateNewUmrah().execute();
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (this, R.array.family_arrays, android.R.layout.simple_spinner_item);
        //set the appearance of widget items that show when open the widget
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter to spinner
        inputfamily.setAdapter(adapter);

        inputfamily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (inputfamily.getItemAtPosition(position).toString().equalsIgnoreCase("Family")) {
                    inputdependent.setVisibility(View.VISIBLE);
                } else {
                    inputdependent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(UmrahBtn.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    class CreateNewUmrah extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UmrahBtn.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            /*pDialog.show();*/
        }

        /**
         * Creating product
         */


        @SuppressWarnings("ResourceType")
        protected String doInBackground(String... args) {

            String email = inputemail.getText().toString();
            String name = inputname.getText().toString();
            String age = inputage.getText().toString();
            String dependent = inputdependent.getText().toString();
            String gender = inputgender.getSelectedItem().toString();
            String country = inputcountry.getSelectedItem().toString();
            String family = inputfamily.getSelectedItem().toString();


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("age", age));
            params.add(new BasicNameValuePair("dependent", dependent));
            params.add(new BasicNameValuePair("gender", gender));
            params.add(new BasicNameValuePair("country", country));
            params.add(new BasicNameValuePair("family", family));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_umrah,
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
                    "Request Created Successfully!", Toast.LENGTH_LONG)
                    .show();
        }

    }
}