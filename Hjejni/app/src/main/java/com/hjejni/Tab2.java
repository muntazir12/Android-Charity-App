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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.app.SearchManager;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hjejni.helper.SQLiteHandler;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Filter;


public class Tab2 extends ListFragment {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object



    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> hajList;

    EditText inputSearch;
    TextView inputtype;
    ListView lv;


    // url to get all products list
    private static String url_all_haj = "http://52.89.129.22/android_login_api/api/get_all_haj.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "haj";
    private static final String TAG_PID = "pid";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_NAME = "name";
    private static final String TAG_AGE = "age";
    private static final String TAG_COUNTRY = "country";


    private SQLiteHandler db;
    // products JSONArray
    JSONArray haj = null;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_2, container, false);

        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String type = user.get("type");

        ImageButton FAB = (ImageButton) v.findViewById(R.id.imageButton);
        inputtype = (TextView) v.findViewById(R.id.type);
        inputtype.setText(type);

        String typec = inputtype.getText().toString();

        if (typec.equals("Beneficial")) {
            FAB.setVisibility(View.VISIBLE);
        }
        else
        {
            FAB.setVisibility(View.GONE);
        }

        FAB.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), HajBtn.class);
                getActivity().startActivity(i);


            }
        });


        return v;


    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        // Hashmap for ListView
        hajList = new ArrayList<HashMap<String, String>>();


        // Loading products in Background Thread
        new LoadAllHaj().execute();


        // Get listview
        View empty = getView().findViewById(R.id.empty);
        lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setEmptyView(empty);

        inputSearch = (EditText) getActivity().findViewById(R.id.search);

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
                        HajView.class);
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
    public class LoadAllHaj extends AsyncTask<String, String, String>{

        EditText inputSearch = (EditText) getActivity().findViewById(R.id.search);
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading requests. Please wait...");
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
            JSONObject json = jParser.makeHttpRequest(url_all_haj, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Haj Request: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    haj = json.getJSONArray(TAG_PRODUCTS);

                    // looping through All Products
                    for (int i = 0; i < haj.length(); i++) {
                        JSONObject c = haj.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String age = c.getString(TAG_AGE);
                        String country = c.getString(TAG_COUNTRY);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_AGE, age);
                        map.put(TAG_COUNTRY, country);

                        // adding HashList to ArrayList
                        hajList.add(map);
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
                            getActivity(), hajList,
                            R.layout.list_haj, new String[]{TAG_PID,
                            TAG_NAME, TAG_AGE, TAG_COUNTRY},
                            new int[]{R.id.pid, R.id.name, R.id.age, R.id.country});
                    // updating listview


                    setListAdapter(adapter);

                    /**
                     * Enabling Search Filter
                     * */

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
