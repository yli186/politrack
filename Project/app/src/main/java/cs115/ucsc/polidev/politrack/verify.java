package cs115.ucsc.polidev.politrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

public class verify extends AppCompatActivity {
    static int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        counter++;

        TextView count = (TextView) findViewById(R.id.counter_text_view);
        count.setText(String.valueOf(counter));

    }

    public void back(View view){
        Intent i=new Intent(this, MapActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
