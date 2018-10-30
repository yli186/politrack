package cs115.ucsc.polidev.politrack;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.NotificationChannel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static String category = "default";
    static int radius = 0;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private String userEmail = (getIntent().getStringExtra("UserEmail"));
    DatabaseReference database;

    //this is the radius variables
    private TextView radiusView;
    private SeekBar radiusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(cs115.ucsc.polidev.politrack.R.layout.activity_main);

        Spinner mySpinner = findViewById(R.id.spinner1);
        //firebase
        database = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        if(isServicesOK()){
            init();
        }
        //code for radius onCreate
        radiusView = findViewById(R.id.radiusView);
        radiusBar = findViewById(R.id.radiusBar);

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==100){
                    radiusView.setText("Radius = " + progress + "+ miles");
                }else if(progress==1){
                    radiusView.setText("Radius = " + progress + " mile");
                }else{
                    radiusView.setText("Radius = " + progress + " miles");
                }
                int index = userEmail.indexOf('@');
                String cu = userEmail.substring(0,index);


                database.child("UserData").child(cu).child("prefRadius").setValue(radius);
                radius = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //end of code for radius
    }

    private void init(){
        final Spinner spinner_category = findViewById(R.id.spinner1);
        Button btnMap = findViewById(cs115.ucsc.polidev.politrack.R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = spinner_category.getSelectedItem().toString();
                System.out.println(category);
                startActivity(new Intent(MainActivity.this, MapActivity.class));
            }
        });
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServiceOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}