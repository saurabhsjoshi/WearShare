package com.sau.wearshare;

import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.sau.wearshare.activities.CountDownActivity;

/**
 * Created by saurabh on 2015-07-11.
 */
public class SendListener extends WearableListenerService {

    private static final String GOT_KEY_PATH = "/got_key";

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(GOT_KEY_PATH))
        {
            Intent cdi = new Intent(this, CountDownActivity.class);
            cdi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cdi.putExtra("code",new String(messageEvent.getData()));
            startActivity(cdi);
        }
    }
}

