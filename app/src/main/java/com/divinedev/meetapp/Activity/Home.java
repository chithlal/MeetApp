package com.divinedev.meetapp.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.divinedev.meetapp.Fragments.MapFragment;
import com.divinedev.meetapp.R;
import com.divinedev.meetapp.Services.BackgroundLocation;
import com.karan.churi.PermissionManager.PermissionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {
    private static final String TAG = "Home";
    PermissionManager permission;



    /**
     * The entry point to Google Play Services.
     */

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setupMap();

                    return true;
                case R.id.navigation_dashboard:
                    Intent it =new Intent(getApplicationContext(),Contacts.class);
                    startActivity(it);

                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        permission=new PermissionManager() {};
        permission.checkAndRequestPermissions(this);



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
       /* buildGoogleApiClient();*/
        Intent i =new Intent(getApplicationContext(),BackgroundLocation.class);
        startForegroundService(i);
        setupMap();

       }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        permission.checkResult(requestCode,permissions, grantResults);
       // buildGoogleApiClient();
    }
  public void setupMap(){
      android.support.v4.app.FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.frame,new MapFragment(getParent()));
      fragmentTransaction.commit();



  }

  }
