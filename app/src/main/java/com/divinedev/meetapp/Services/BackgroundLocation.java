package com.divinedev.meetapp.Services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divinedev.meetapp.Activity.Home;
import com.divinedev.meetapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BackgroundLocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 5; // 10 meters
    public GoogleApiClient mGoogleApiClient;
    private LocationListener listener;
    private Location mLastLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    NotificationManager mNotificationManager;
    Location mobileLocation;
    RequestQueue requestQueue;
    HashMap<String,String> hashMap = new HashMap<>();
   // HttpParse httpParse = new HttpParse();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        // Toast.makeText(getApplicationContext(),"Location ",Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        createChannel();
       // startForegroundService();
       // showNotification();
        //noinspection MissingPermission
       startForegroundService();
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    togglePeriodicLocationUpdates();
                }
            }, 2000);

        }



/*
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,listener);
        mobileLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if(locationManager != null){
            //noinspection MissingPermission

        }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)getApplicationContext(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }
    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }
    public void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("ABC", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
       // displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        // Assign the new location
        mLastLocation = location;
        SharedPreferences sp= getApplicationContext().getSharedPreferences("LAST_LOCATION",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        // Toast.makeText(getApplicationContext(), "Location changed!"+location.getLatitude()+" "+location.getLongitude(),
        // Toast.LENGTH_SHORT).show();
       /* Intent i = new Intent("location_update");
        i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
        sendBroadcast(i);*/
        editor.putString("LAT",String.valueOf(location.getLatitude()));
        editor.putString("LON",String.valueOf(location.getLongitude()));
        editor.apply();
        Log.i(TAG, "onLocationChanged: "+location.getLatitude()+","+location.getLongitude());
        insertV(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
        // Displaying the new location on UI
      /*  displayLocation();
        Insert();*/


    }
//    private void displayLocation() {
//        DataBaseHandler db=new DataBaseHandler(getApplicationContext());
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mLastLocation = LocationServices.FusedLocationApi
//                .getLastLocation(mGoogleApiClient);
//
//        if (mLastLocation != null) {
//            double latitude = mLastLocation.getLatitude();
//            double longitude = mLastLocation.getLongitude();
//            int len=generateList().length;
//            int[] pos=generateList();
//            for (int i=0;i<len;i++){
//                Location wloc=new Location("");
//                wloc.setLatitude(db.getLat(pos[i]));
//                wloc.setLongitude(db.getLon(pos[i]));
//                if(checkNot(wloc,mLastLocation)) {
//                    sendNotification(pos[i]);
//                    if (checkRange(wloc, mLastLocation)) {
//                        activatePro(pos[i]);
//                    }
//                }
//            }
//
//           /* Toast.makeText(getApplicationContext(), "Location changed! displayed"+latitude+" "+longitude,
//                    Toast.LENGTH_SHORT).show();*/
//
//
//
//        } else {
//
//            Toast.makeText(getApplicationContext(), "Location failed",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void sendNotification(int po) {
//        AudioManager myAudioManager;
//        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        DataBaseHandler db=new DataBaseHandler(getApplicationContext());
//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent intent = new Intent(this, BackgroundActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Notification notification = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("AUTOMODE")
//                .setContentText("I am going to activate "+db.getName(po)+" Mode")
//                .setContentIntent(pendingNotificationIntent)
//                .setSmallIcon(R.drawable.p2)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(db.getName(po)))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .build();
//        notificationManager.notify(0, notification);
//
//    }
//
//
//    private void activatePro(int position) {
//        DataBaseHandler db=new DataBaseHandler(getApplicationContext());
//        AudioManager myAudioManager;
//        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        if(db.getNvol(position)==0&&db.getRvol(position)==0&&db.getAvol(position)==0&&db.getMvol(position)==0){
//            myAudioManager.setMode(AudioManager.RINGER_MODE_SILENT);
//        }
//        else {
//            myAudioManager.setStreamVolume(AudioManager.STREAM_RING,db.getRvol(position),AudioManager.FLAG_SHOW_UI);
//            myAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,db.getAvol(position),AudioManager.FLAG_SHOW_UI);
//            myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,db.getMvol(position),AudioManager.FLAG_SHOW_UI);
//            myAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,db.getNvol(position),AudioManager.FLAG_SHOW_UI);
//
//
//
//        }
//        Toast.makeText(getApplicationContext(),"Profile changed",Toast.LENGTH_SHORT).show();
//    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text


            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d("ABC", "Periodic location updates started!");

        } else {
            // Changing the button text


            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d("ABC", "Periodic location updates stopped!");
        }
    }
   /* private int[] generateList(){
        int length=0;

        DataBaseHandler db=new DataBaseHandler(getApplicationContext());
        for (int i=0;i<db.getProfileCount();i++){
            if(db.getStatus(i+1).equals("Activated")){
                length++;
            }
        }
        int[] pos=new int[length];
        int count=0;
        for (int i=0;i<db.getProfileCount();i++){
            if(db.getStatus(i+1).equals("Activated")){
                pos[count]=i+1;
                count++;
            }
        }
        return pos;
    }
    private boolean checkRange(Location loc1,Location loc2){
        // Toast.makeText(getApplicationContext(),"Location check "+loc1.distanceTo(loc2),Toast.LENGTH_SHORT).show();

        if(loc1.distanceTo(loc2)<100){
            //Toast.makeText(getApplicationContext(),"Location reached "+loc1.distanceTo(loc2),Toast.LENGTH_SHORT).show();

            return true;
        }
        else return false;
    }
    private boolean checkNot(Location L1,Location L2){
        if(L1.distanceTo(L2)<200){

            return true;
        }
        else return false;
    }

    public void Insert(){

        *//* phpDb pb =new phpDb(getApplicationContext());*//*
        new Send().execute();
    }
    class Send extends AsyncTask<String, Void,Long > {

        @Override
        protected void onPreExecute() {
            SharedPreferences sp= getApplicationContext().getSharedPreferences("LAST_LOCATION",MODE_PRIVATE);
            final SharedPreferences.Editor editor=sp.edit();
            // progresss=new ProgressDialog(context);
            super.onPreExecute();
            //  progresss = ProgressDialog.show(context,"Registration On Progress ","Please wait...");
            //  progresss.setCancelable(false);
        }
        @SuppressWarnings("deprecation")
        @Override
        protected Long doInBackground(String... urls) {

            SharedPreferences sp= getApplicationContext().getSharedPreferences("LAST_LOCATION",MODE_PRIVATE);
            final SharedPreferences.Editor editor=sp.edit();

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://dvynedevelopers.com/update.php");



            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("Email",sp.getString("email"," ")));
                nameValuePairs.add(new BasicNameValuePair("Long",sp.getString("LON"," ")));
                nameValuePairs.add(new BasicNameValuePair("Lat", sp.getString("LAT"," ")));


                //  nameValuePairs.add(new BasicNameValuePair("image",getStringImage(bitmap)));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));



                // Execute HTTP Post Request

                HttpResponse response = httpclient.execute(httppost);


            } catch (Exception e) {

                Toast.makeText(getApplicationContext(),"Unexpected Error",Toast.LENGTH_LONG).show();


            }
            return null;

        }

        protected void onPostExecute(Long result) {
            //   progresss.dismiss();
            //    Toast.makeText(getApplicationContext(),"Upadte succefull",Toast.LENGTH_LONG).show();




        }
    }*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannel(){
        NotificationChannel channel = new NotificationChannel("default",
                getApplicationContext().getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(channel);
    }
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), Home.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(Home.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext(),
                "default")
                .setContentTitle("MeetApp is running")
                .setContentText("Location Update")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0, notificationBuilder.build());
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService()
    {
       // Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.");

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create notification builder.
       Notification.Builder builder = new Notification.Builder(this,"default");

        // Make notification show big text.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("Music player implemented by foreground service.");
        bigTextStyle.bigText("Android foreground service is a android service which can run in foreground always, it can be controlled by user via notification.");
        // Set big text style.

        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ic_chat_bubble_black_24dp);
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_activity_24dp);
        builder.setLargeIcon(largeIconBitmap);
        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX);
        // Make head-up notification.
        builder.setFullScreenIntent(pendingIntent, true);

        // Add Play button intent in notification.
        Intent playIntent = new Intent(this, BackgroundLocation.class);
      //  playIntent.setAction(ACTION_PLAY);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);


        // Add Pause button intent in notification.
        Intent pauseIntent = new Intent(this, BackgroundLocation.class);
      //  pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPrevIntent);


        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        startForeground(1, notification);
    }
    public void insertData(final String lat, final String lon){
        final String ServerURL="https://dvynedevelopers.000webhostapp.com/InsertData.php";
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String NameHolder = "Chithlal" ;
                String EmailHolder = "chithlalkrishna.dev@gmail.com" ;
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Log.i(TAG, "doInBackground: time:"+currentDateTimeString);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("name", NameHolder));
                nameValuePairs.add(new BasicNameValuePair("phone", "8714136584"));
                nameValuePairs.add(new BasicNameValuePair("email", EmailHolder));
                nameValuePairs.add(new BasicNameValuePair("lat", lat));
                nameValuePairs.add(new BasicNameValuePair("lon", lon));
               /*nameValuePairs.add(new BasicNameValuePair("t",currentDateTimeString));
                nameValuePairs.add(new BasicNameValuePair("lone", lon));*/
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;
                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(ServerURL);

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                     httpResponse = httpClient.execute(httpPost);

                    httpEntity = httpResponse.getEntity();
                    Log.i(TAG, "doInBackground: inserting");


                } catch (ClientProtocolException e) {
                    Log.e(TAG, "doInBackground: ",e );

                } catch (IOException e) {

                }
                return httpResponse.toString();
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);
                Log.i(TAG, "onPostExecute: php"+result);

                Toast.makeText(getApplicationContext(), "Data Submit Successfully", Toast.LENGTH_LONG).show();

            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute(lat, lon);
    }
    public void insertV(final String lat, final String lon){
        final String NameHolder = "Chithlal" ;
        final String EmailHolder = "chithlalkrishna@gmail.com" ;
        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.i(TAG, "doInBackground: time:"+currentDateTimeString);
        final String ServerURL="https://dvynedevelopers.000webhostapp.com/InsertData.php";
        requestQueue = Volley.newRequestQueue(getBaseContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {



                        // Showing response message coming from server.
                        //Toast.makeText(getApplicationContext(), ServerResponse, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onResponse: "+ServerResponse);
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
                params.put("name", NameHolder);
                params.put("phone", "8714136584");
                params.put("email", EmailHolder);
                params.put("lat",lat);
                params.put("lon",lon);
                params.put("t",currentDateTimeString);

                return params;
            }

        };

        // Creating RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
        Toast.makeText(getApplicationContext(), "inserting", Toast.LENGTH_LONG).show();

    }

}



