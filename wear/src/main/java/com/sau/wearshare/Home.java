package com.sau.wearshare;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sau.wearshare.adapters.HomePagerAdapter;
import com.sau.wearshare.fragments.home.ReceiveFragment;
import com.sau.wearshare.fragments.home.SendFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Home extends Activity {
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private static final String MOBILE_PATH = "/mobile";

    private GoogleApiClient googleApiClient;
    private String nodeId;


    private GridViewPager mPager;
    private SendFragment mSendFragment;
    private ReceiveFragment mReceiveFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initGoogleApiClient();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setUI();
            }
        });
    }

    private void setUI(){
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageCount(1);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(mPager);
        mSendFragment = new SendFragment();
        mReceiveFragment = new ReceiveFragment();
        List<Fragment> pages = new ArrayList<>();
        pages.add(mSendFragment);
        pages.add(mReceiveFragment);
        final HomePagerAdapter adapter = new HomePagerAdapter(getFragmentManager(), pages);
        mPager.setAdapter(adapter);

    }




    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }



    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (googleApiClient != null && !(googleApiClient.isConnected() || googleApiClient.isConnecting()))
                    googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

                List<Node> nodes = result.getNodes();

                if (nodes.size() > 0)
                    nodeId = nodes.get(0).getId();

                googleApiClient.disconnect();
            }
        }).start();
    }

    private void initGoogleApiClient() {
        googleApiClient = getGoogleApiClient(this);
        retrieveDeviceNode();
    }


    private void sendToast() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (googleApiClient != null && !(googleApiClient.isConnected() || googleApiClient.isConnecting()))
                        googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(googleApiClient, nodeId, MOBILE_PATH, "Hello".getBytes()).await();
                    googleApiClient.disconnect();
                }
            }).start();
        }
    }


}
