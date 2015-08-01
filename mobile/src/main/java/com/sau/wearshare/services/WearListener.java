package com.sau.wearshare.services;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.sau.wearshare.Secret;
import com.sau.wearshare.activities.CameraActivity;
import com.sau.wearshare.sdk.SendTask;
import com.sau.wearshare.sdk.Task;
import com.sau.wearshare.utils.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 15-07-09.
 */
public class WearListener extends WearableListenerService {

    private static final String TAG = "WearListener";

    private static final String CLICK_PATH = "/click_photo";
    private static final String SEND_PICTURE_PATH = "/send_photo";
    private static final String CANCEL_PICTURE_PATH = "/cancel_photo";
    private static final String GOT_KEY_PATH = "/got_key";
    private static final String DOWNLOAD_STARTED_PATH = "/download_started";

    private static final long CONNECTION_TIME_OUT_MS = 100;
    private String node;

    private GoogleApiClient mGoogleApiClient;
    private SendTask sendTask;

    @Override
    public void onCreate() {
        super.onCreate();
        Task.init(Secret.API_KEY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        retrieveDeviceNode();
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
        Logger.LOGD(TAG, "Received message: " + messageEvent.getPath());
        if (messageEvent.getPath().equals(CLICK_PATH))
            takePicture();
        else if (messageEvent.getPath().equals(SEND_PICTURE_PATH))
            sendPictureFromData();
        else if (messageEvent.getPath().equals(CANCEL_PICTURE_PATH))
            cancelSendPicture();

    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient != null && !(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()))
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                List<Node> nodes = result.getNodes();

                if (nodes.size() > 0)
                    node = nodes.get(0).getId();
            }
        }).start();
    }



    private void takePicture(){
        Intent dialogIntent = new Intent(this, CameraActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    private void sendPictureCode(String code){
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, GOT_KEY_PATH, code.getBytes()).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }

    private boolean messageSent = false;
    private void sendDownloadStartedMessage(){
        if (!messageSent) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, DOWNLOAD_STARTED_PATH, new byte[0]).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Logger.LOGD(TAG, "Download started Message Sent");
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Logger.LOGD(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
            messageSent = true;
        }

    }

    private void sendPictureFromData(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                sendTask = new SendTask(getApplicationContext(), new File[]{new File(getFilesDir() + "/click.jpg")});
                sendTask.setOnTaskListener(new Task.OnTaskListener() {
                    @Override
                    public void onNotify(int state, int detailedState, Object obj) {
                        if (state == SendTask.State.PREPARING) {
                            if (detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY) {
                                String key = (String) obj;
                                if (key != null)
                                    sendPictureCode(key);
                            }
                        } else if(state == SendTask.State.TRANSFERRING) {
                            SendTask.FileInfo fileState = (SendTask.FileInfo)obj;
                            if(fileState != null) {
                                Logger.LOGD(TAG, String.format("%s: %s/%s",
                                        fileState.getFile().getName(),
                                        fileState.getTransferSize(), fileState.getTotalSize()));
                                sendDownloadStartedMessage();
                            }
                        } else if (state == SendTask.State.FINISHED) {
                            switch(detailedState) {
                                case SendTask.DetailedState.FINISHED_SUCCESS:
                                    Logger.LOGD(TAG, "Transfer finished (success)");
                                    break;
                                case SendTask.DetailedState.FINISHED_CANCEL:
                                    Logger.LOGD(TAG, "Transfer finished (canceled)");
                                    break;
                                case SendTask.DetailedState.FINISHED_ERROR:
                                    Logger.LOGD(TAG, "Transfer finished (error!)");
                                    break;
                            }
                        }
                        else if(state == SendTask.State.ERROR) {
                            switch(detailedState) {
                                case SendTask.DetailedState.ERROR_SERVER:
                                    Logger.LOGD(TAG, "Netork/Server Error");
                                    break;
                                case SendTask.DetailedState.ERROR_NO_REQUEST:
                                    Logger.LOGD(TAG, "Timeout!");
                                    break;
                                case SendTask.DetailedState.ERROR_NO_EXIST_FILE:
                                    Logger.LOGD(TAG,"No exist files!");
                                    break;
                            }
                        }
                    }
                });
                sendTask.start();
            }
        });
    }

    private void cancelSendPicture(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(sendTask != null)
                    sendTask.cancel();
            }
        });

    }

}
