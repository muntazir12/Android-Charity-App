package com.hjejni;

/**
 * Created by muntazir on 01/10/15.
 */
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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


public class Tab1 extends ListFragment {

    // Progress Dialog
    private ProgressDialog pDialog;

    private SessionManager session;


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
    private static final String TAG_MESSAGE = "message";

    TextView inputremail;
    EditText inputSearch;

    // products JSONArray
    JSONArray message = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_1, container, false);


        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(getActivity().getApplicationContext());

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String email = user.get("email");
        inputremail = (TextView) v.findViewById(R.id.remail);
        inputremail.setText(email);

        Button FAB = (Button) v.findViewById(R.id.button);
        FAB.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), SentMessage.class);
                getActivity().startActivity(i);


            }
        });

        return v;


    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {



        // Hashmap for ListView
        messageList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllMessage().execute();

        // Get listview
        View empty = getView().findViewById(R.id.empty);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setEmptyView(empty);

        inputSearch = (EditText) getActivity().findViewById(R.id.search1);

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
                Intent in = new Intent(getActivity().getApplicationContext(),
                        MessageView.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid
                );

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
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
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        }

    }



    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllMessage extends AsyncTask<String, String, String> {

        String email = inputremail.getText().toString();
        EditText inputSearch = (EditText) getActivity().findViewById(R.id.search1);
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
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


                        String remail = c.getString(TAG_REMAIL);
                        String id, sname, message, semail;
                        if (remail.equals(email) && !remail.isEmpty()) {


                            // Storing each json item in variable
                            id = c.getString(TAG_PID);
                            sname = c.getString(TAG_SNAME);
                            semail = c.getString(TAG_SEMAIL);
                            message = c.getString(TAG_MESSAGE);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();


                            // adding each child node to HashMap key => value
                            map.put(TAG_PID, id);
                            map.put(TAG_SNAME, sname);
                            map.put(TAG_SEMAIL, semail);
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

            if(getActivity() == null)
                return;

            // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                        final SimpleAdapter adapter = new SimpleAdapter(
                                getActivity(), messageList,
                                R.layout.list_message, new String[]{TAG_PID, TAG_SNAME, TAG_SEMAIL, TAG_MESSAGE},
                                new int[]{R.id.pid, R.id.sname, R.id.semail, R.id.message});
                        // updating listview

                            setListAdapter(adapter);

                    inputSearch.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                            adapter.getFilter().filter(cs);

                        }

                        @Override
                        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                                      int arg3) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void afterTextChanged(Editable arg0) {
                            // TODO Auto-generated method stub
                        }
                    });
                }

            });

        }

    }
}