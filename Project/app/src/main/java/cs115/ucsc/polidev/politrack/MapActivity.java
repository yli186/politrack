package cs115.ucsc.polidev.politrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

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

    //Request code for location permission request
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //Flag indicating whether a requested permission has been denied after returning in
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    //current location
    LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    static final int REQUEST_LOCATION = 1;
    //save current user mail as id.
    private String currUserMail;
    //firebase
    DatabaseReference database;
    //fetch all report from db
    ArrayList<Report> rpt;

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
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));
                        }
                    }
                });


        //get curr user mail.
        currUserMail  = (getIntent().getStringExtra("UserEmail"));
        //firebase
        database = FirebaseDatabase.getInstance().getReference();
        //fetch report arraylist
        rpt = new ArrayList<Report>();
        rpt = fetchrpt();
        // load up all the relevant markers in onCreate
    }

    @Override
    public void onResume(){
        super.onResume();
        rpt = fetchrpt();
    }

    public ArrayList<Report> fetchrpt(){
        final ArrayList<Report> list = new ArrayList<>();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference().child("ReportData");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Report tdm = postSnapshot.getValue(Report.class);
                    System.out.println(tdm.type);
                    list.add(tdm);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        System.out.println("HELLO " + list.size());
        return list;

    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        //When map loads, it will go to the specified coordinates.
        //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Location location = getLocation();
        //onLocationChanged(location);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));
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
    public void onMyLocationClick(@NonNull Location location) {
        GoogleMap map = mMap;
        String category_name = MainActivity.category;
        if(category_name.equals("Police")){
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_police)));
        }else if(category_name.equals("Protest/Strikes")){
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_protest)));
        }else if(category_name.equals("Bench")){
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_bench)));
        }else if(category_name.equals("Public Bathroom")){
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_toilet)));
        }else if(category_name.equals("Water Fountains")){
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(category_name + " " +
                    Calendar.getInstance().getTime()).icon(BitmapDescriptorFactory.fromResource(R.drawable.indicator_fountain)));
        }
        Toast.makeText(this, "Current location reported.", Toast.LENGTH_LONG).show();

        //kevin's edit
        // Justin, fill all those with actual value calls from google map API.
        UploadReport(category_name, String.valueOf(Calendar.getInstance().getTime()), location.getLatitude(), location.getLongitude(), LoginActivity.nAcc, 1);
    }

    private void UploadReport(String typ, String t, double lng, double lat, String rpU, int c){
        cs115.ucsc.polidev.politrack.Report report = new Report(typ,t,lng,lat,rpU,c);
        rpt.add(report);
        database.child("ReportData").setValue(rpt);

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
}
