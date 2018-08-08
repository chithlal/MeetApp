package com.divinedev.meetapp.Activity;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divinedev.meetapp.Adapter.ContactAdapter;
import com.divinedev.meetapp.Database.ContactDB;
import com.divinedev.meetapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Contacts extends AppCompatActivity {
    private static final String TAG = "My";
    Cursor cursor;
    List<com.divinedev.meetapp.Helper.Contacts> StoreContacts=new ArrayList<>();
    RecyclerView recyclerView;
    ContactAdapter contactAdapter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        recyclerView = (RecyclerView) findViewById(R.id.contact_list);
        //

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

       // contactAdapter = new ContactAdapter(listContact(),getApplicationContext());

        insertV();


    }
    public void GetContactsIntoArrayList(){
        String name,phonenumber;
        com.divinedev.meetapp.Helper.Contacts cont;

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //Log.i(TAG, "GetContactsIntoArrayList: "+name+" : "+phonenumber);
           // String phonenumber1=removeChar(phonenumber,'+');
            String phonenumber1= phonenumber.replace("+","");
            String phonenumber2= phonenumber1.replace("-","");
            String phonenumber3= phonenumber2.replace(" ","");
            Log.i(TAG, "GetContactsIntoArrayList: "+phonenumber3);

            cont=new com.divinedev.meetapp.Helper.Contacts(name,phonenumber3,false,null);
            StoreContacts.add(cont);
        }
        Collections.sort(StoreContacts);
       // contactAdapter.notifyDataSetChanged();
        cursor.close();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.id_update_contact:
               update();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void update() {
       new AsyncCaller().execute();

    }
    public List<com.divinedev.meetapp.Helper.Contacts> listContact(){
        ContactDB db=new ContactDB(getApplicationContext());
        List<com.divinedev.meetapp.Helper.Contacts> contacts=new ArrayList<>();
        contacts=db.getAll();
        return contacts;
    }
    public JSONObject getJSONObject(com.divinedev.meetapp.Helper.Contacts contacts) {
        JSONObject obj = new JSONObject();
        try {

            obj.put("Name", contacts.getName());
            obj.put("phone", contacts.getPhone());
            //Log.i(TAG, "getJSONObject: "+obj.toString());
        } catch (JSONException e) {
            Log.i(TAG, "getJSONObject: JSONException: "+e.getMessage());
        }
        return obj;
    }
    public String getJsonArray() {
        ContactDB db=new ContactDB(getApplicationContext());
       List<com.divinedev.meetapp.Helper.Contacts> contact=db.getAll();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < contact.size(); i++) {
            jsonArray.put(getJSONObject(contact.get(i)));
            //Log.i(TAG, "getJSONObject: "+getJSONObject(contact.get(i)).toString());

        }
       Log.i(TAG, "getJsonArray: "+jsonArray.toString());
        return jsonArray.toString();

    }
    public void insertV(){
        //RequestQueue requestQueue;

        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.i(TAG, "doInBackground: time:"+currentDateTimeString);
        final String ServerURL="https://dvynedevelopers.000webhostapp.com/contactCheck.php";
       // requestQueue = Volley.newRequestQueue(getBaseContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {



                        // Showing response message coming from server.
                        Toast.makeText(getApplicationContext(), ServerResponse, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onResponse: "+ServerResponse);
                        checkContact();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.


                        // Showing error message if something goes wrong.
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: "+volleyError.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();

                // Adding All values to Params.
                params.put("contacts", getJsonArray());


                return params;
            }

        };

        // Creating RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
        //Toast.makeText(getApplicationContext(), "inserting", Toast.LENGTH_LONG).show();

    }
    private void checkContact() {
        String JSON_URL="https://dvynedevelopers.000webhostapp.com/contact_results.json";
        //getting the progressbar
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //making the progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        //creating a string request to send request to the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ContactDB db=new ContactDB(getApplicationContext());
                        //hiding the progressbar after completion
                        progressBar.setVisibility(View.INVISIBLE);


                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray heroArray = obj.getJSONArray("contacts");

                            //now looping through all the elements of the json array
                            for (int i = 0; i < heroArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject heroObject = heroArray.getJSONObject(i);

                                //creating a hero object and giving them the values from json object
                               /* com.divinedev.meetapp.Helper.Contacts hero = new com.divinedev.meetapp.Helper.Contacts(heroObject.getString("name"), heroObject.getString("imageurl"));
*/
                               db.update(heroObject.getString("phone"),1);
                                //adding the hero to herolist
                               // heroList.add(hero);

                            }

                            //creating custom adapter object
                           // ListViewAdapter adapter = new ListViewAdapter(heroList, getApplicationContext());

                            //adding the adapter to listview
                          //  listView.setAdapter(adapter);
                            contactAdapter = new ContactAdapter(listContact(),getApplicationContext());
                            recyclerView.setAdapter(contactAdapter);
                            contactAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    private static String removeChar(String s, char c) {
        StringBuffer buf = new StringBuffer(s.length());
        buf.setLength(s.length());
        int current = 0;
        for (int i=0; i<s.length(); i++){
            char cur = s.charAt(i);
            if(cur != c) buf.setCharAt(current++, cur);
        }
        return buf.toString();
    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {


        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            GetContactsIntoArrayList();

            ContactDB db=new ContactDB(getApplicationContext());
            db.truncateDB();

            for (int i=0;i<StoreContacts.size();i++){
                db.insertContact(StoreContacts.get(i));
            }
            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
         progressBar.setVisibility(View.GONE);

        }

    }

}
