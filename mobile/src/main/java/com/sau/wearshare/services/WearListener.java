package com.sau.wearshare.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
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
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.sau.wearshare.R;
import com.sau.wearshare.Secret;
import com.sau.wearshare.activities.CameraActivity;
import com.sau.wearshare.model.FileObject;
import com.sau.wearshare.sdk.ReceiveTask;
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
    private static final String RECEIVE_FILE_PATH = "/receive_path";

    private static final String EXPLORE_HOME_PATH = "/explore_home";
    private static final String EXPLORE_FOLDER_PATH = "/explore_folder";
    private static final String EXPLORE_DATA_PATH = "/explore_data";
    private static final String EXPLORE_SENT_PATH = "/explore_sent";



    private static final long CONNECTION_TIME_OUT_MS = 100;
    private String node;


    private GoogleApiClient mGoogleApiClient;

    private SendTask sendTask;
    private ReceiveTask receiveTask;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private int mNotificationId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Task.init(Secret.API_KEY);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        retrieveDeviceNode();
        IntentFilter filter = new IntentFilter();
        filter.addAction("cancel_upload");
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
        else if (messageEvent.getPath().equals(RECEIVE_FILE_PATH))
            receiveFile(new String(messageEvent.getData()));
        else if (messageEvent.getPath().equals(EXPLORE_HOME_PATH))
            sendExploreFiles(new String(messageEvent.getData()), true);
        else if (messageEvent.getPath().equals(EXPLORE_FOLDER_PATH))
            sendExploreFiles(new String(messageEvent.getData()), false);
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


    private void sendExploreFiles(String path, boolean isHome){
        String root = Environment.getExternalStorageDirectory().toString();
        if(isHome)
            path = root;
        else
            path = root + path;

        Logger.LOGD(TAG, path);


        File f = new File(path);
        File file[] = f.listFiles();
        if(file != null)
        {
            for(int i = 0; i < file.length; i++){
                FileObject temp;
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(EXPLORE_DATA_PATH);

                //Don't show hidden files
                if(!file[i].getName().startsWith(".")){
                    temp = new FileObject(file[i].getName(), file[i].isFile());
                    temp.getDataMap(putDataMapReq.getDataMap());
                    PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).await();
                }
            }

        }

        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, EXPLORE_SENT_PATH, new byte[0]);

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
        buildNotification("Sending File", "Upload in progress");
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
                                int percentage = (int) (fileState.getTransferSize() * 100.0 /fileState.getTotalSize());
                                mNotificationBuilder.setProgress(100,
                                        percentage, false);
                                mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                                Logger.LOGD(TAG, String.format("%s: %s/%s",
                                        fileState.getFile().getName(),
                                        fileState.getTransferSize(), fileState.getTotalSize()));
                                sendDownloadStartedMessage();
                            }
                        } else if (state == SendTask.State.FINISHED) {
                            String message = "Done";
                            switch(detailedState) {
                                case SendTask.DetailedState.FINISHED_SUCCESS:
                                    message = "File transfer complete!";
                                    Logger.LOGD(TAG, "Transfer finished (success)");
                                    break;
                                case SendTask.DetailedState.FINISHED_CANCEL:
                                    message = "File transfer cancelled!";
                                    Logger.LOGD(TAG, "Transfer finished (canceled)");
                                    break;
                                case SendTask.DetailedState.FINISHED_ERROR:
                                    message = "File transfer error!";
                                    Logger.LOGD(TAG, "Transfer finished (error!)");
                                    break;
                            }
                            mNotificationBuilder.setContentText(message)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                        }
                        else if(state == SendTask.State.ERROR) {
                            String message = "Error";
                            switch(detailedState) {
                                case SendTask.DetailedState.ERROR_SERVER:
                                    message = "Network error occurred!";
                                    Logger.LOGD(TAG, "Network/Server Error");
                                    break;
                                case SendTask.DetailedState.ERROR_NO_REQUEST:
                                    message = "Timeout!";
                                    Logger.LOGD(TAG, "Timeout!");
                                    break;
                                case SendTask.DetailedState.ERROR_NO_EXIST_FILE:
                                    message = "File does not!";
                                    Logger.LOGD(TAG,"No exist files!");
                                    break;
                            }

                            mNotificationBuilder.setContentText(message)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                        }
                    }
                });
                sendTask.start();
            }
        });
    }

    public void cancelSendPicture(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (sendTask != null)
                    sendTask.cancel();
            }
        });

    }

    private void buildNotification(String title, String message) {
        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher);
    }

    private void receiveFile(final String key){
        buildNotification("Receiving File", "Download in progress");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                receiveTask = new ReceiveTask(getApplicationContext(), key);
                receiveTask.setOnTaskListener(new Task.OnTaskListener() {
                    @Override
                    public void onNotify(int state, int detailedState, Object obj) {
                        if (state == ReceiveTask.State.PREPARING) {
                            mNotificationBuilder.setProgress(100,
                                    0, false);
                            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                            if (detailedState == ReceiveTask.DetailedState.PREPARING_UPDATED_FILE_LIST) {
                                Task.FileInfo[] fileInfoList = (Task.FileInfo[])obj;
                                for(Task.FileInfo file : fileInfoList) {
                                    Logger.LOGD(TAG,String.format("%s: %d bytes",
                                            file.getPathName(), file.getTotalSize()));
                                }
                            }
                        }

                        else if (state == ReceiveTask.State.TRANSFERRING) {
                            ReceiveTask.FileInfo fileState = (ReceiveTask.FileInfo) obj;
                            if (fileState != null) {
                                int percentage = (int) (fileState.getTransferSize() * 100.0 /fileState.getTotalSize());
                                mNotificationBuilder.setProgress(100,
                                        percentage, false);
                                mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                                Logger.LOGD(TAG, String.format("%s => %s/%s",
                                        fileState.getFile().getName(),
                                        fileState.getTransferSize(), fileState.getTotalSize()));
                            }
                        }

                        else if (state == ReceiveTask.State.FINISHED) {
                            String message = "Done!";
                            switch (detailedState) {
                                case ReceiveTask.DetailedState.FINISHED_SUCCESS:
                                    message = "Download successful!";
                                    Logger.LOGD(TAG,"Transfer finished (success)");
                                    break;
                                case ReceiveTask.DetailedState.FINISHED_CANCEL:
                                    message = "Download cancelled!";
                                    Logger.LOGD(TAG,"Transfer Cancelled");
                                    break;
                                case ReceiveTask.DetailedState.FINISHED_ERROR:
                                    message = "Download error! Please try again.";
                                    Logger.LOGD(TAG,"Transfer finished (error!)");
                                    break;
                            }
                            mNotificationBuilder.setContentText(message)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                        }

                        else if (state == ReceiveTask.State.ERROR) {
                            String message = "Error!";
                            switch (detailedState) {
                                case ReceiveTask.DetailedState.ERROR_SERVER:
                                    message = "Network Error!";
                                    Logger.LOGD(TAG, "Network or Server Error!");
                                    break;
                                case ReceiveTask.DetailedState.ERROR_NO_EXIST_KEY:
                                    message = "Invalid Key!";
                                    Logger.LOGD(TAG,"Invalid Key!");
                                    break;
                                case ReceiveTask.DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH:
                                    message = "Invalid download path!";
                                    Logger.LOGD(TAG, "Invalid download path");
                                    break;
                            }
                            mNotificationBuilder.setContentText(message)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
                        }

                    }
                });

                receiveTask.start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
