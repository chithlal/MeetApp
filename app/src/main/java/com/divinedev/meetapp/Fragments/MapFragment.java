package com.divinedev.meetapp.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.divinedev.meetapp.Helper.Contacts;
import com.divinedev.meetapp.Helper.Marker;
import com.divinedev.meetapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class MapFragment extends Fragment {
    MapView mMapView;
    private GoogleMap googleMap;
    private View mCustomMarkerView;
    private ImageView mMarkerImageView;
    View rootView;
    List<Marker> markers=new ArrayList<>();

    Activity activity;

    public MapFragment() {
        // Required empty public constructor
        activity = getActivity();
    }


    @SuppressLint("ValidFragment")
    public MapFragment(Activity activity) {

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_map, container, false);

       // plotMap();
        mCustomMarkerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
        mMarkerImageView = (ImageView) mCustomMarkerView.findViewById(R.id.profile_image);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        getNearBy();
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }
    private Bitmap getMarkerBitmapFromView(View view, @DrawableRes int resId) {

        mMarkerImageView.setImageResource(resId);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }
    public void getNearBy(){
        final String TAG ="Nearby";
        SharedPreferences sp=getActivity().getSharedPreferences("LAST_LOCATION",Context.MODE_PRIVATE);
        final double lat=Double.valueOf(sp.getString("LAT","0.0"));
        final double lon=Double.valueOf(sp.getString("LON","0.0"));

        final String ServerURL="https://dvynedevelopers.000webhostapp.com/getLocation.php";
        // requestQueue = Volley.newRequestQueue(getBaseContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURL,
                new Response.Listener<String>() {


                    @Override
                    public void onResponse(String ServerResponse) {



                        // Showing response message coming from server.
                        Toast.makeText(getActivity(), ServerResponse, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onResponse: getnearby "+ServerResponse);

                        //checkContact();
                        plotMap();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.


                        // Showing error message if something goes wrong.
                        Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: "+volleyError.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();

                // Adding All values to Params.
                params.put("contacts", getJsonArray());
                params.put("mylat",String.valueOf(lat));
                params.put("mylon",String.valueOf(lon));


                return params;
            }

        };

        // Creating RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
        //Toast.makeText(getApplicationContext(), "inserting", Toast.LENGTH_LONG).show();

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
        ContactDB db=new ContactDB(getActivity());
        List<Contacts> contact=db.getActive();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < contact.size(); i++) {
            jsonArray.put(getJSONObject(contact.get(i)));
            //Log.i(TAG, "getJSONObject: "+getJSONObject(contact.get(i)).toString());

        }
        Log.i(TAG, "getJsonArray: "+jsonArray.toString());
        return jsonArray.toString();

    }
    private void plotMap() {
        String JSON_URL="https://dvynedevelopers.000webhostapp.com/latlong.json";
        //getting the progressbar
        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //making the progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        //creating a string request to send request to the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // ContactDB db=new ContactDB(getApplicationContext());
                        //hiding the progressbar after completion
                        progressBar.setVisibility(View.INVISIBLE);


                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray heroArray = obj.getJSONArray("latlon");

                            //now looping through all the elements of the json array
                            for (int i = 0; i < heroArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject heroObject = heroArray.getJSONObject(i);

                                //creating a hero object and giving them the values from json object
                                /* com.divinedev.meetapp.Helper.Contacts hero = new com.divinedev.meetapp.Helper.Contacts(heroObject.getString("name"), heroObject.getString("imageurl"));
                                 */
                                //db.update(heroObject.getString("phone"),1);
                                //adding the hero to herolist
                                // heroList.add(hero);
                                Marker marker=new Marker(heroObject.getString("phone"),heroObject.getString("lat"),heroObject.getString("lon"));
                                markers.add(marker);
                                Log.i(TAG, "onResponse: latlon"+marker.getLat());


                            }

                            //creating custom adapter object
                            // ListViewAdapter adapter = new ListViewAdapter(heroList, getApplicationContext());

                            //adding the adapter to listview
                            loadMap();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
    public  void loadMap(){

        mMapView.onResume();
        // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                SharedPreferences sp=getActivity().getSharedPreferences("LAST_LOCATION",Context.MODE_PRIVATE);
                double lat=Double.valueOf(sp.getString("LAT","0.0"));
                double lon=Double.valueOf(sp.getString("LON","0.0"));

                LatLng myloc = new LatLng(lat,lon);
                googleMap.addMarker(new MarkerOptions().position(myloc).title("we are here").snippet("Marker Description")
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, R.drawable.ic_activity_24dp))));

                for (int i=0;i<markers.size();i++){
                    Marker marker=markers.get(i);
                    double lati=Double.valueOf(marker.getLat());
                    double longi=Double.valueOf(marker.getLon());
                    String phone=marker.getPhone();
                    LatLng friends = new LatLng(lati,longi);
                    googleMap.addMarker(new MarkerOptions().position(friends).title("Friend "+String.valueOf(i)).snippet(phone)
                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, R.drawable.ic_activity_24dp))));
                }


              /* From URL
               Glide.with(getApplicationContext()).
                        load(ImageUrl)
                        .asBitmap()
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(mDummyLatLng)
                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, bitmap))));
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDummyLatLng, 13f));


                            }
                        });*/

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myloc).zoom(13).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

    }

}
