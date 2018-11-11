package cs115.ucsc.polidev.politrack;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends AppCompatActivity
            implements
            OnMapReadyCallback,
            OnMyLocationClickListener,
            OnMyLocationButtonClickListener,
            ActivityCompat.OnRequestPermissionsResultCallback {

    TextToSpeech toSpeech; //for text to speech notification

    //Request code for location permission request
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    static String category = "default";
    //Flag indicating whether a requested permission has been denied after returning in
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    //current location
    LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    static final int REQUEST_LOCATION = 1;
    static int spinnerPosition = -1;
    //save current user mail as id.
    private String currUserMail;
    //firebase
    DatabaseReference database;
    //fetch all report from db
    ArrayList<Report> rpt;
    //store lat and long of current location
    static double lat2 = 0.0;
    static double lon2 = 0.0;
    // flag for fetch report
    private boolean LOCK_REPORT_DATA_CHANGE = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));
                            drawCircle(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                });

        //get curr user mail.
        currUserMail  = (getIntent().getStringExtra("UserEmail"));

        //firebase
        database = FirebaseDatabase.getInstance().getReference();

        // setting default display
        database.child("UserData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User tdm = postSnapshot.getValue(User.class);
                    if (tdm.userEmail.equals(MainActivity.userEmail)) {
                        category = tdm.category;
                        // refresh activity
                        LOCK_REPORT_DATA_CHANGE = true;
                        refreshMap();
                        LOCK_REPORT_DATA_CHANGE = false;
                        // change position value based on category (probably better solution but this works for now)
                        //
                        final Spinner mySpinner = findViewById(R.id.spinner1);
                        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                                R.array.names, android.R.layout.simple_spinner_item);
                        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mySpinner.setAdapter(myAdapter);
                        // set default spinner text to be saved category preference
                        int spinnerPosition = myAdapter.getPosition(category);
                        mySpinner.setSelection(spinnerPosition);
                        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                category = (String) parent.getItemAtPosition(position);
                                int index = MainActivity.userEmail.indexOf('@');
                                String cu = MainActivity.userEmail.substring(0, index);
                                database.child("UserData").child(cu).child("category").setValue(category);
                                LOCK_REPORT_DATA_CHANGE = true;
                                refreshMap();
                                LOCK_REPORT_DATA_CHANGE = false;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        //testing API level thing
        System.out.println("SDDK " +Build.VERSION.RELEASE);

        toSpeech=new TextToSpeech(MapActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS)
                {
                    toSpeech.setLanguage(Locale.US);
                    toSpeech.setPitch(1);
                    toSpeech.setSpeechRate(1);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public ArrayList<Report> fetchrpt(){
        final ArrayList<Report> list = new ArrayList<>();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference().child("ReportData");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Report tdm = postSnapshot.getValue(Report.class);
                    list.add(tdm);
                }
                System.out.println("WOW DATA "+currUserMail);
                try{
                    checkPreferences(list);
                }catch(Exception e){
                    System.out.println("ERROR 'boolean java.lang.String.equals(java.lang.Object)' on a null object reference'");
                }
                System.out.println("NNIHAO fetchrpt "+ LOCK_REPORT_DATA_CHANGE);
                if(!LOCK_REPORT_DATA_CHANGE){
                    notifySighting();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return list;
    }

    private Circle drawCircle(LatLng latLng){
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius((MainActivity.radius)*1609.34)
                .fillColor(0x33FF0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        return mMap.addCircle(options);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        // initialize refresh here
    }

     //Enables the My Location layer if the fine location permission has been granted.
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    // Will use this to report current location
    @Override
    public void onMyLocationClick(@NonNull Location location){
        addIndicator(location.getLatitude(), location.getLongitude());
        //kevin's edit
        UploadReport(category, String.valueOf(Calendar.getInstance().getTime()), location.getLatitude(), location.getLongitude(), LoginActivity.nAcc, 1);
        // temporarily make the map refresh when a new report is made. Can relocate this ones we figure out how to pull data anytime.
        checkPreferences(rpt);
        // --------------------------------------------------------
    }

    public void addIndicator(double latitude, double longitude){
        GoogleMap map = mMap;
        String category_name = category;
        if(category_name.equals("Police")){
            map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                    longitude)).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_police)));
        }else if(category_name.equals("Protest/Strikes")){
            map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                    longitude)).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_protest)));
        }else if(category_name.equals("Bench")){
            map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                    longitude)).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_bench)));
        }else if(category_name.equals("Public Bathroom")){
            map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                    longitude)).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_toilet)));
        }else if(category_name.equals("Water Fountains")){
            map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                    longitude)).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_fountain)));
        }
        Toast.makeText(this, "Refreshed.", Toast.LENGTH_LONG).show();
    }

    private void UploadReport(String typ, String t, double lat, double lng, String rpU, int c){
        cs115.ucsc.polidev.politrack.Report report = new Report(typ,t,lat,lng,rpU,c);
        rpt.add(report);
        database.child("ReportData").setValue(rpt);
    }

    public boolean checkRadius(final double lat1, final double lon1){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // check the two points here.
                            lat2 = location.getLatitude();
                            lon2 = location.getLongitude();
                        }
                    }
                });
        //do the formula to get the distance between two points. Compare the distance to radius.
        double R = 6371000;
        double var1 = Math.toRadians(lat1);
        double var2 = Math.toRadians(lat2);
        double var3 = Math.toRadians(lat2-lat1);
        double var4 = Math.toRadians(lon2-lon1);

        double a = Math.sin(var3/2) * Math.sin(var3/2) +
                Math.cos(var1) * Math.cos(var2) *
                        Math.sin(var4/2) * Math.sin(var4/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        d = d/1609.344; //convert to miles.
        double radius = MainActivity.radius;
        if(radius>=d){
            return true;
        }else{
            return false;
        }
    }

    public void checkPreferences(ArrayList<Report> rpt){
        System.out.println("TESST "+category);
        String category_name = category;
        //loop to see if requirements are met.
        for(int i=0; i<rpt.size(); i++){
            //check if category name is correct
            if(category_name.equals(rpt.get(i).type) ){
                // check if radius is correct
                if(checkRadius(rpt.get(i).latit, rpt.get(i).longit)){
                    System.out.println("JOSHH "+i+" "+rpt.get(i).type);
                    addIndicator(rpt.get(i).latit, rpt.get(i).longit);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void notifySighting() {
        int NOTIFICATION_ID = 123;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channel_id = "channel_1";
        CharSequence name = "channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channel_id);

        Intent i = new Intent(this, MapActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);

        Intent verify = new Intent(this, verify.class);
        verify.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent VerifyPendingIntent = PendingIntent.getActivity(this, 0, verify, PendingIntent.FLAG_UPDATE_CURRENT);

        String category_name = category;
        builder.setContentTitle(category_name + " sighted!");

        builder.setContentText("Someone reported a " + category_name + " sighting in your area.");

        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.addAction(R.mipmap.ic_launcher,"verify",VerifyPendingIntent);

        builder.setAutoCancel(true);

        Notification notification = builder.build();

        notificationManager.notify(NOTIFICATION_ID, notification);
        speak ("Someone reported a " + category_name + " sighting in your area.");
    }


    private void speak(String text){
        toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    protected void onDestroy() {
        if (toSpeech != null) {
            toSpeech.stop();
            toSpeech.shutdown();
        }
        super.onDestroy();
    }

    public void refreshMap(){
        try{
            mMap.clear();
        }catch(Exception e){
            System.out.println("ERROR: void com.google.android.gms.maps.GoogleMap.clear()' on a null object reference");
        }
        rpt = fetchrpt();
    }
}
