package com.sau.wearshare.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sau.wearshare.R;
import com.sau.wearshare.adapters.HomePagerAdapter;
import com.sau.wearshare.fragments.browser.ExploreFragment;
import com.sau.wearshare.fragments.browser.SelectionFragment;
import com.sau.wearshare.fragments.browser.SendFilesFragment;
import com.sau.wearshare.models.DataHolder;
import com.sau.wearshare.models.FileObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 2015-08-01.
 */
public class FileActivity extends Activity implements DataApi.DataListener, MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "FileActivity";

    private GridViewPager mPager;
    private Handler mHandler;

    private GoogleApiClient mGoogleApiClient;
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private String node;

    List<Fragment> pages;
    DotsPageIndicator dotsPageIndicator;


    private ExploreFragment mExploreFragment;
    private SelectionFragment selectionFragment;
    private SendFilesFragment sendFilesFragment;

    private boolean mFragmentShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        initGoogleApiClient();
        mHandler = new Handler();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                pages = new ArrayList<>();
                setUI();
            }
        });
    }

    private void setUI(){
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageCount(0);
        mExploreFragment = new ExploreFragment();
        pages.add(mExploreFragment);
        final HomePagerAdapter adapter = new HomePagerAdapter(getFragmentManager(), pages);
        mPager.setAdapter(adapter);
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

    public void sendFiles(){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == 11){
                String result=data.getStringExtra("filename");
                if(mExploreFragment != null)
                    mExploreFragment.goAhead(result);
            }
            if (resultCode == 99) {
                addSelectedFile(DataHolder.selectedItem);
                updateUi();
            }
        }
    }

    private void updateUi(){
        if(!mFragmentShowing){
            mPager.setOffscreenPageCount(2);
            selectionFragment = new SelectionFragment();
            sendFilesFragment = new SendFilesFragment();
            pages.add(selectionFragment);
            pages.add(sendFilesFragment);
            dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
            dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
            dotsPageIndicator.setPager(mPager);
            dotsPageIndicator.setVisibility(View.VISIBLE);
            mFragmentShowing = true;
        }
        else {
            selectionFragment.updateList();
        }

        moveToPage(1);
    }
    private void moveToPage(int num){
        mPager.setCurrentItem(0, num, true);
    }

    private void addSelectedFile(FileObject fileObject){
        DataHolder.selectedItems.add(fileObject);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
