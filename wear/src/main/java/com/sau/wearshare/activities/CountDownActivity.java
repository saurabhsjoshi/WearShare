package com.sau.wearshare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CircularButton;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sau.wearshare.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 2015-07-11.
 */
public class CountDownActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener {
    private TextView txt_count;
    private CircularButton btn_cancel;

    private GoogleApiClient mGoogleApiClient;

    private static final long CONNECTION_TIME_OUT_MS = 100;
    private static final String CANCEL_PICTURE_PATH = "/cancel_photo";
    private static final String DOWNLOAD_STARTED_PATH = "/download_started";

    private String node;

    private static final String FORMAT = "%02d:%02d";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);
        initGoogleApiClient();
        txt_count = (TextView) findViewById(R.id.txt_count);
        btn_cancel = (CircularButton) findViewById(R.id.btn_cancel);

        btn_cancel.setColor(getResources().getColor(R.color.btn_talk_blue));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ((TextView) findViewById(R.id.txt_code)).setText(extras.getString("code"));
        }

        final CountDownTimer timer = new CountDownTimer(588000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                txt_count.setText(""+
                        String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                        ));
            }

            @Override
            public void onFinish() {
                finish();
            }
        };

        timer.start();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                sendCancelMessage();
            }
        });

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
    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
        retrieveDeviceNode();
    }

    private void sendCancelMessage(){
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, CANCEL_PICTURE_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        finish();
                    }
                }
        );

    }

    private void cleanUp(){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Transfer Started");
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(DOWNLOAD_STARTED_PATH)){
            cleanUp();
            mGoogleApiClient.disconnect();
        }
    }
}
