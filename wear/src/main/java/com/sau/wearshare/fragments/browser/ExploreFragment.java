package com.sau.wearshare.fragments.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
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
import com.sau.wearshare.R;
import com.sau.wearshare.adapters.ExploreListAdapter;
import com.sau.wearshare.models.FileObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 2015-08-01.
 */
public class ExploreFragment extends Fragment implements WearableListView.ClickListener,
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener{

    private static final String TAG = "Explore Fragment";
    private TextView txt_cur_path;
    private WearableListView lst_files;

    private ArrayList<String> full_path;


    private List<FileObject> files;
    private ArrayList<FileObject> temp;
    private ExploreListAdapter exploreListAdapter;

    private GoogleApiClient mGoogleApiClient;
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private String node;

    private static final String EXPLORE_HOME_PATH = "/explore_home";
    private static final String EXPLORE_FOLDER_PATH = "/explore_folder";
    private static final String EXPLORE_DATA_PATH = "/explore_data";
    private static final String EXPLORE_SENT_PATH = "/explore_sent";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_explore, container, false);
        files = new ArrayList<>();
        temp = new ArrayList<>();
        exploreListAdapter = new ExploreListAdapter(getActivity(), files);

        full_path = new ArrayList<>();
        full_path.add("home");

        initGoogleApiClient();

        txt_cur_path = (TextView) view.findViewById(R.id.txt_title);
        lst_files = (WearableListView) view.findViewById(R.id.lst_items);

        (view.findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        lst_files.setAdapter(exploreListAdapter);
        lst_files.setGreedyTouchMode(true);
        lst_files.setClickListener(this);
        return view;
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        String folder = files.get(viewHolder.getAdapterPosition()).getFilename();
        goAhead(folder);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private void goBack(){
        if(full_path.size() == 1)
            setCurrentPath("home");

        else
        {
            full_path.remove(full_path.size()-1);
            setCurrentPath(full_path.get(full_path.size()-1));
        }

    }

    private void setCurrentPath(String path){
        txt_cur_path.setText(path);
    }

    private void goAhead(String folder){
        full_path.add(folder);
        setCurrentPath(folder);
    }

    private void loadHome(){

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(EXPLORE_DATA_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    temp.add(new FileObject(dataMap));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
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
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
        retrieveDeviceNode();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        sendExploreMessage(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void sendExploreMessage(Boolean isHome){
        String path = EXPLORE_FOLDER_PATH;
        if(isHome)
            path = EXPLORE_HOME_PATH;

        String ex_path = "";
        for(String s:full_path) ex_path += "/" + s;

        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, path, ex_path.getBytes()).setResultCallback(
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals(EXPLORE_SENT_PATH)){
            Collections.sort(temp, new Comparator<FileObject>() {
                @Override
                public int compare(FileObject fileObject, FileObject t1) {
                    return fileObject.getFilename().compareTo(t1.getFilename());
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    files.clear();
                    files.addAll(temp);
                    temp.clear();
                    exploreListAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
