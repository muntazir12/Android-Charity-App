package com.hjejni;

/**
 * Created by muntazir on 01/10/15.
 */
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v7.widget.SearchView;

import com.hjejni.helper.SQLiteHandler;
import com.hjejni.helper.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class SentMessage extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;


    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> messageList;



    // url to get all products list
    private static String url_all_message = "http://52.89.129.22/android_login_api/api/get_all_message.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "message";
    private static final String TAG_PID = "pid";
    private static final String TAG_REMAIL = "remail";
    private static final String TAG_SEMAIL = "semail";
    private static final String TAG_SNAME = "sname";
    private static final String TAG_RNAME = "rname";
    private static final String TAG_MESSAGE = "message";

    TextView inputremail;
    Toolbar toolbar;


    // products JSONArray
    JSONArray message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sent_message);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle(R.string.sent_msg);

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String email = user.get("email");
        inputremail = (TextView) findViewById(R.id.semail);
        inputremail.setText(email);


        // Hashmap for ListView
        messageList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllMessage().execute();

        // Get listview
        View empty = findViewById(R.id.empty);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setEmptyView(empty);

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        RMessageView.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid
                );

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

        Button FAB = (Button) findViewById(R.id.button);
        FAB.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(SentMessage.this, MainActivity.class);
                startActivity(i);


            }
        });
    }



    // Response from second Product Activity will be here

    // Response from Edit Product Activity
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



    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllMessage extends AsyncTask<String, String, String> {

        String email = inputremail.getText().toString();
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SentMessage.this);
            pDialog.setMessage("Loading Messages. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            /*pDialog.show();*/



        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_message, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Message Request: ", json.toString());


            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // products found
                    // Getting Array of Products
                    message = json.getJSONArray(TAG_PRODUCTS);


                    // looping through All Products
                    for (int i = 0; i < message.length(); i++) {
                        JSONObject c = message.getJSONObject(i);


                        String semail = c.getString(TAG_SEMAIL);
                        String id, rname, message, remail;
                        if (semail.equals(email) && !semail.isEmpty()) {


                            // Storing each json item in variable
                            id = c.getString(TAG_PID);
                            rname = c.getString(TAG_RNAME);
                            remail = c.getString(TAG_REMAIL);
                            message = c.getString(TAG_MESSAGE);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();


                            // adding each child node to HashMap key => value
                            map.put(TAG_PID, id);
                            map.put(TAG_RNAME, rname);
                            map.put(TAG_REMAIL, remail);
                            map.put(TAG_MESSAGE, message);


                            // adding HashList to ArrayList
                            messageList.add(map);
                        }
                    }

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
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            SentMessage.this, messageList,
                            R.layout.list_smessage, new String[]{TAG_PID, TAG_RNAME, TAG_REMAIL, TAG_MESSAGE},
                            new int[]{R.id.pid, R.id.sname, R.id.remail, R.id.message});
                    // updating listview

                    setListAdapter(adapter);
                }

            });

        }

    }
}