package cs115.ucsc.polidev.politrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewUserActivity extends Activity {
    EditText NameRegister;
    EditText UsernameRegister;
    EditText PasswordRegister;

    static String NameStore;
    static String UsernameStore;
    static String PasswordStore;
    static String ChoppedUser;

    //firebase
    FirebaseAuth mAuth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        NameRegister = (EditText) findViewById(R.id.NameRegister);
        UsernameRegister = (EditText) findViewById(R.id.UsernameRegister);
        PasswordRegister = (EditText) findViewById(R.id.PasswordRegister);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void LoginReturn(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void writeNewUser(){
        cs115.ucsc.polidev.politrack.User user = new User(NameStore, UsernameStore, PasswordStore, 0, "Police", 0, true, true);
        database.child("UserData").child(ChoppedUser).setValue(user);
    }
    public void Return (View v){

        this.NameStore = NameRegister.getText().toString();
        this.UsernameStore = UsernameRegister.getText().toString();
        this.PasswordStore = PasswordRegister.getText().toString();
        int index = UsernameStore.indexOf('@');
        ChoppedUser = UsernameStore.substring(0,index);

        mAuth.createUserWithEmailAndPassword(UsernameStore, PasswordStore)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(cs115.ucsc.polidev.politrack.NewUserActivity.this, "Authentication succeed",
                                    Toast.LENGTH_SHORT).show();
                            writeNewUser();
                            SuccessReturn();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(cs115.ucsc.polidev.politrack.NewUserActivity.this, "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }

    public void SuccessReturn(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
