package cs115.ucsc.polidev.politrack;

import android.content.BroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;


import static cs115.ucsc.polidev.politrack.MapActivity.lastKnownReports;

public class verify extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //cs115.ucsc.polidev.politrack.Report extras = (cs115.ucsc.polidev.politrack.Report)intent.getSerializableExtra("report");
        //extras.inc_count();
        //lastKnownReports.add(extras);

        context.sendBroadcast(new Intent("verify"));

    }
}
