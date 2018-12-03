package cs115.ucsc.polidev.politrack;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.Serializable;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity
            implements
            OnMapReadyCallback,
            OnMyLocationClickListener,
            OnMyLocationButtonClickListener,
            ActivityCompat.OnRequestPermissionsResultCallback {

    TextToSpeech toSpeech; //for text to speech notification

    // shake feature
    private SensorManager sm;
    private float acelVal; // CURRENT ACCELERATION VALUE AND GRAVITY.
    private float acelLast; // LAST ACCELERATION VALUE AND GRAVITY.
    private float shake; // ACCELERATION VALUE differ FROM GRAVITY.
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
    //Store current coordinates
    static double latitude = 0.0;
    static double longitude = 0.0;
    //Store reference to reports
    static List<Report> lastKnownReports = new ArrayList<Report>();
    //Store a flag to indicated when to send notification
    static int SKIP_INITIAL_ONDATACHANGE = 0;
    //Store length of report list to see if previous report list and new report list is same length
    static int REPORT_LENGTH = 0;
    static boolean verify_flag = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        registerReceiver(broadcastReceiver, new IntentFilter("verify"));


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
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            //drawCircle(new LatLng(location.getLatitude(), location.getLongitude()));
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
                        // change position value based on category (probably better solution but this works for now)
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
                                // clear map and reload with new category
                                refreshMap();
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

        //new fetchReport
        database.child("ReportData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // access database to fill map with relevant markers.
                try{
                    mMap.clear();
                }catch(Exception e){
                    System.out.println("ERROR: void com.google.android.gms.maps.GoogleMap.clear()' on a null object reference");
                }
                System.out.println("HOLY "+dataSnapshot.child("1").child("type").getValue());
                // clear list of reports and fill with new reports from the snapshot
                REPORT_LENGTH = lastKnownReports.size(); // save the previous length of the report list to see if a new report was added
                int NEW_REPORT_LENGTH = (int) dataSnapshot.getChildrenCount(); // create a local int variable to store the length of report list
                lastKnownReports.clear();
                fillMap(dataSnapshot);
                if(SKIP_INITIAL_ONDATACHANGE==0){
                    SKIP_INITIAL_ONDATACHANGE++; // skip initial report notification
                }else if(REPORT_LENGTH<NEW_REPORT_LENGTH){
                    // check if the category is the same
                    if(category.equals(dataSnapshot.child(String.valueOf(NEW_REPORT_LENGTH-1)).child("type").getValue())){
                        long count = (long) dataSnapshot.child(String.valueOf(NEW_REPORT_LENGTH-1)).child("count").getValue();
                        // check if notification is turned on
                        if(MainActivity.notificationOn){
                            checkReportRadius((double) dataSnapshot.child(String.valueOf(NEW_REPORT_LENGTH-1)).child("latit").getValue(), (double) dataSnapshot.child(String.valueOf(NEW_REPORT_LENGTH-1)).child("longit").getValue(), NEW_REPORT_LENGTH-1, count);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if (shake > 14 && MainActivity.shakeOn){
                getCurrentLatitudeLongitude(0.0, 0.0, "Shake", 0, Long.toString(System.currentTimeMillis()));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void fillMap(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            Report reportInformation = ds.getValue(Report.class);
            lastKnownReports.add(reportInformation);
            // get current coordinates and call check preference
            getCurrentLatitudeLongitude(reportInformation.getLatit(), reportInformation.getLongit(), reportInformation.getType(), reportInformation.getCount(), reportInformation.getTime());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
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
        // reset SKIP_INITIAL_ONDATACHANGE to avoid getting notified on something you reported.
        SKIP_INITIAL_ONDATACHANGE = 0;
        UploadReport(category, Long.toString(System.currentTimeMillis()), location.getLatitude(), location.getLongitude(), LoginActivity.nAcc);
    }

    private void UploadReport(String typ, String t, double lat, double lng, String rpU){
        // check if the location you just reported is within range of another report. If it is then update count. If not, send a new report.
        if(checkDuplicateReport(typ, t, lat, lng)){ // send updated reports to the database
            database.child("ReportData").setValue(lastKnownReports);
            Toast.makeText(this, "Existing sighting verified!", Toast.LENGTH_SHORT).show();
        }else{
            List<String> reportedUser = Arrays.asList(LoginActivity.nAcc);
            cs115.ucsc.polidev.politrack.Report report = new Report(typ,t,lat,lng,reportedUser,1);
            lastKnownReports.add(report);
            database.child("ReportData").setValue(lastKnownReports);
            Toast.makeText(this, "New sighting reported!", Toast.LENGTH_SHORT).show();
        }
    }

    public void update_database(){
        database.child("ReportData").setValue(lastKnownReports);
    }

    public void checkPreferences(double lat1, double lon1, double lat2, double lon2, String type, int count, String time){
        if(lat1 == 0.0 && lon1 == 0.0 && type.equals("Shake") && count == 0){// lazy solution but I think it works
            System.out.println("DOES THIS WORK");
            if(checkDuplicateReport(type, time, lat2, lon2)){ // if there are duplicate reports then update count and update report list
                database.child("ReportData").setValue(lastKnownReports);
            }else{ // if there are no duplicates, then add new report and update report list
                UploadReport(category, Long.toString(System.currentTimeMillis()), lat2, lon2, LoginActivity.nAcc);
            }
        }else{
            String category_name = category;
            double radius = MainActivity.radius;
            System.out.println("checkPreferences "+radius);
            //check if category name is correct
            if(category_name.equals(type)){
                // check if radius is correct
                if(checkRadius(lat1, lon1, lat2, lon2, radius)){
                    if (checkTime(time, MainActivity.time)){
                        addIndicator(lat1, lon1, count, time);
                    }
                }
            }
        }
    }

    public boolean checkRadius(final double lat1, final double lon1, final double lat2, final double lon2, final double radius){
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
        if(radius >= d) {
            return true;
        }else if(radius == 100 && d > radius){
            return true;
        }else{
            return false;
        }
    }

    public  boolean checkTime(String reported_time, int hour_preference){
        long reported_time_long = Long.parseLong(reported_time); //convert reported time to long. Stored as string
        long current_time = System.currentTimeMillis(); // store current time
        long hour = 3600000*Long.valueOf(hour_preference); //this is epoche
        current_time -= hour; //update current time by subtracting the number of hours to it
        System.out.println(current_time);
        System.out.println(reported_time_long);
        if(hour_preference == 0){
            return false;
        }else if(hour_preference == 100) {
            return true;
        }else if(current_time >= reported_time_long){
            return true;
        }else{
            return false;
        }
    }

    public void addIndicator(double latitude, double longitude, int count, String time){
        GoogleMap map = mMap;
        String category_name = category;
        long reported_time_long = Long.parseLong(time);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        if(category_name.equals("Police")){
            if(count == 1){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.police_1)));
            }else if (count == 2){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.police_2)));
            }else{
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.police_3)));
            }
        }else if(category_name.equals("Protest/Strikes")){
            if(count == 1){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.protest_1)));
            }else if (count == 2){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.protest_2)));
            }else{
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.protest_3)));
            }
        }else if(category_name.equals("Bench")){
            if(count == 1){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.bench_1)));
            }else if (count == 2){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.bench_2)));
            }else{
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.bench_3)));
            }
        }else if(category_name.equals("Public Bathroom")){
            if(count == 1){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.toilet_1)));
            }else if (count == 2){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.toilet_2)));
            }else{
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.toilet_3)));
            }
        }else if(category_name.equals("Water Fountains")){
            if(count == 1){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.fountain_1)));
            }else if (count == 2){
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.fountain_2)));
            }else{
                map.addMarker(new MarkerOptions().position(new LatLng(latitude,
                        longitude)).title("Count: " + count + ". Last Reported:  " +
                        sdf.format(new Date(reported_time_long)) + ".").icon(BitmapDescriptorFactory.fromResource(R.drawable.fountain_3)));
            }
        }
        Toast.makeText(this, "Refreshed!", Toast.LENGTH_SHORT).show();
    }

    public void getCurrentLatitudeLongitude(final double lat1, final double lon1, final String type, final int count, final String time) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // check the two points here.
                            MapActivity.latitude = location.getLatitude();
                            MapActivity.longitude = location.getLongitude();
                            // call checkPreference once you get the current coordinates
                            checkPreferences(lat1, lon1, latitude, longitude, type, count, time);
                        }
                    }
                });
    }

    public void checkReportRadius(final double lat1, final double lon1, final int report_length, final long count) {
        System.out.println("DOES IT WORK?");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // check the two points here.
                            if(checkRadius(lat1, lon1, location.getLatitude(), location.getLongitude(), MainActivity.radius)){
                                notifySighting(report_length, count);
                            }
                        }
                    }
                });
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

    //send notification to user, takes in an index and count for verify action button
    public void notifySighting(int index, long count) {
        int NOTIFICATION_ID = 123;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channel_id = "channel_1";
        CharSequence name = "channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        //setup for different android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channel_id);

        Intent i = new Intent(this, MapActivity.class); //intent for notification
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);

        Intent verify = new Intent(this, verify.class); //verify action button

        verify.putExtra("index",index); //pass in
        verify.putExtra("count", count);

        verify.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent VerifyPendingIntent = PendingIntent.getBroadcast(this, 0, verify, PendingIntent.FLAG_UPDATE_CURRENT);

        String category_name = category;

        //settings for notification
        builder.setContentTitle(category_name + " sighted!");
        builder.setContentText("Someone reported a " + category_name + " sighting in your area.");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.addAction(R.mipmap.ic_launcher,"verify",VerifyPendingIntent);
        builder.setAutoCancel(true);

        Notification notification = builder.build(); //build notification

        notificationManager.notify(NOTIFICATION_ID, notification);//send notification
        speak ("Someone reported a " + category_name + " sighting in your area.");//text-to-speech

    }

    //for receiving broadcast sent back from the verify intent
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            verify_flag = true;
            int index = intent.getIntExtra("index",0);
            int count = intent.getIntExtra("count",1);
            //Check if this user already reported a sighting for this location
            System.out.println("TESTING " + lastKnownReports.get(index).reportedUser);
            if(!checkDuplicateUser(lastKnownReports.get(index).reportedUser)){
                lastKnownReports.get(index).addUser(LoginActivity.nAcc); // add user to list
                lastKnownReports.get(index).setCount(); // increment count
                lastKnownReports.get(index).setTime(Long.toString(System.currentTimeMillis()));
                database.child("ReportData").setValue(lastKnownReports);
            }
            NotificationManagerCompat.from(context).cancel(123); //cancel the notification when action buttton clicked
        }
    };


    //text-to-speech
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
        unregisterReceiver(broadcastReceiver);
    }

    public boolean checkDuplicateReport(String type, String time, double latitude, double longitude){
        System.out.println("BRUH + "+lastKnownReports);
        for(int i=0; i<lastKnownReports.size(); i++){ // iterate through reports
            if(type.equals(lastKnownReports.get(i).getType())){ // continue if category type is the same
                if(checkRadius(latitude, longitude, lastKnownReports.get(i).getLatit(), lastKnownReports.get(i).getLongit(), 0.1)){ // continue if the two reports are within 0.1 miles
                    //check if user is already in the list
                    System.out.println("BRUH "+lastKnownReports.get(i).getReportedUser());
                    if(!checkDuplicateUser(lastKnownReports.get(i).getReportedUser())){
                        //change value of count
                        lastKnownReports.get(i).setCount();
                        lastKnownReports.get(i).setTime(time);
                        lastKnownReports.get(i).addUser(LoginActivity.nAcc);
                        return true;
                    }else{
                        return true; // skip duplicate report
                    }
                }
            }
        }
        return false;
    }

    public boolean checkDuplicateUser(List<String> userList){
        for(int i = 0; i < userList.size(); i++){
            userList.get(i);
            if(LoginActivity.nAcc.equals(userList.get(i))){
                return true;
            }
        }
        return false;
    }

    public void refreshMap(){
        try{
            mMap.clear();
        }catch(Exception e){
            System.out.println("ERROR: void com.google.android.gms.maps.GoogleMap.clear()' on a null object reference");
        }
        for(int i=0; i<lastKnownReports.size(); i++){
            // call checkPreference once you get the current coordinates
            checkPreferences(lastKnownReports.get(i).getLatit(), lastKnownReports.get(i).getLongit(), latitude, longitude, lastKnownReports.get(i).getType(), lastKnownReports.get(i).getCount(), lastKnownReports.get(i).getTime());
        }
    }
}
