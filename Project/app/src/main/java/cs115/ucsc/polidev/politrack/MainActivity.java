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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.NotificationChannel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static int radius = 0;
    static int time = 0;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    static String userEmail;
    DatabaseReference database;

    //this is the radius variables
    private TextView radiusView;
    private SeekBar radiusBar;

    //this is the time variables
    private TextView timeView;
    private SeekBar timeBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(cs115.ucsc.polidev.politrack.R.layout.activity_main);
        userEmail = (getIntent().getStringExtra("UserEmail"));

        //firebase
        database = FirebaseDatabase.getInstance().getReference();

        if(isServicesOK()){
            init();
        }
        //code for radius onCreate
        radiusView = findViewById(R.id.radiusView);
        radiusBar = findViewById(R.id.radiusBar);
        //code for time onCreate
        timeView = findViewById(R.id.timeView);
        timeBar = findViewById(R.id.timeBar);
        //load radius and time
        database.child("UserData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    User tdm = postSnapshot.getValue(User.class);
                    if(tdm.userEmail.equals(userEmail)){
                        radius = tdm.prefRadius;
                        time =tdm.prefTime;
                        System.out.println("saved radius" +radius);
                        System.out.println("saved time" +time);
                        if(radius==100){
                            radiusView.setText("Notification Radius = " + radius + "+ miles");
                        }else if(radius==1){
                            radiusView.setText("Notification Radius = " + radius + " mile");
                        }else{
                            radiusView.setText("Notification Radius = " + radius + " miles");
                        }
                        if(time==100){
                            timeView.setText("Notification Time = " + time + "+ hours");
                        }else if(time==1){
                            timeView.setText("Notification Time = " + time + " hour");
                        }else{
                            timeView.setText("Notification Time = " + time + " hours");
                        }
                        radiusBar.setProgress(radius);
                        timeBar.setProgress(time);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==100) {
                    timeView.setText("Time = " + i + " + hours");
                }else if(i ==1) {
                    timeView.setText("Time = " + i + "  hour");
                }else{
                    timeView.setText("Time = " +i +" hours");
                }
                //push time
                time = i;
                int index = userEmail.indexOf('@');
                String cu = userEmail.substring(0,index);
                database.child("UserData").child(cu).child("prefTime").setValue(time);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
                //push radius
                radius = progress;
                int index = userEmail.indexOf('@');
                String cu = userEmail.substring(0,index);
                database.child("UserData").child(cu).child("prefRadius").setValue(radius);
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
        Button btnMap = findViewById(cs115.ucsc.polidev.politrack.R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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