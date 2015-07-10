package com.sau.wearshare.services;

import android.widget.Toast;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by saurabh on 15-07-09.
 */
public class WearListener extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(this, new String(messageEvent.getData()), Toast.LENGTH_LONG).show();
    }
}
