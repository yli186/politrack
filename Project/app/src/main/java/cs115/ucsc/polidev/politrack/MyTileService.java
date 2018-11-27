package cs115.ucsc.polidev.politrack;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class MyTileService extends TileService {

    private final String LOG_TAG = "MyTileService";
    private final int STATE_ON = 1;
    private final int STATE_OFF = 0;
    private int toggleState = STATE_ON;

    @Override
    public void onTileAdded(){ Log.d(LOG_TAG, "onTileAdded");
    }

    @Override
    public void onTileRemoved(){
        Log.d(LOG_TAG, "onTileRemoved");
    }

    @Override
    public void onStartListening(){
        Log.d(LOG_TAG, "onStartListening");
    }

    @Override
    public void onStopListening(){
        Log.d(LOG_TAG, "onStopListening");
    }

    @Override
    public void onClick(){
        Log.d(LOG_TAG, "onClick state = " + Integer.toString(getQsTile().getState()));
        Icon icon;
        if(toggleState == STATE_ON){
            toggleState = STATE_OFF;
            icon = Icon.createWithResource(getApplicationContext(), R.drawable.bench);
            MainActivity.toggleSwitch(toggleState);
        }else{
            toggleState = STATE_ON;
            icon = Icon.createWithResource(getApplicationContext(), R.drawable.toilet);
            MainActivity.toggleSwitch(toggleState);
        }

        getQsTile().setIcon(icon);
        getQsTile().updateTile();
    }
}
