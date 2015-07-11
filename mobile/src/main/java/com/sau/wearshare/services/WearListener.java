package com.sau.wearshare.services;

import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.sau.wearshare.Secret;
import com.sau.wearshare.activities.CameraActivity;
import com.sau.wearshare.sdk.SendTask;
import com.sau.wearshare.sdk.Task;
import com.sau.wearshare.utils.Logger;

import java.io.File;

/**
 * Created by saurabh on 15-07-09.
 */
public class WearListener extends WearableListenerService {
    private static final String TAG = "WearListener";

    private static final String CLICK_PATH = "/click_photo";
    private static final String SEND_PICTURE_PATH = "/send_photo";

    private static final String GOT_KEY_PATH = "got_key";

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
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Logger.LOGD(TAG, "Received message.");
        if (messageEvent.getPath().equals(CLICK_PATH))
            takePicture();
        else if (messageEvent.getPath().equals(SEND_PICTURE_PATH))
            sendPictureFromData();

    }


    private void takePicture(){
        Intent dialogIntent = new Intent(this, CameraActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    private void sendPictureFromData(){
        Task.init(Secret.API_KEY);
        SendTask sendTask = new SendTask(this, new File[]{new File(getFilesDir() + "/temp.png")});

        sendTask.setOnTaskListener(new Task.OnTaskListener() {
            @Override
            public void onNotify(int state, int detailedState, Object obj) {
                if(state == SendTask.State.PREPARING) {
                    if(detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY) {
                        String key = (String)obj;
                        if(key != null) {

                        }
                    }
                }
            }
        });
    }





}
