package cs115.ucsc.polidev.politrack;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

public class verify extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int ind = intent.getIntExtra("index", 0); //get the extra  parameter
        int count = intent.getIntExtra("count", 1);

        Intent v_intent = new Intent("verify"); //a new intent indicating verify clicked
        v_intent.putExtra("index", ind); //with the same parameter
        v_intent.putExtra("count", count);
        context.sendBroadcast(v_intent);  //send it back to mapActivity to do the incrementation
    }
}
