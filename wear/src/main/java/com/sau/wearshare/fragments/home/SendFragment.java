package com.sau.wearshare.fragments.home;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sau.wearshare.R;
import com.sau.wearshare.activities.ClickActivity;
import com.sau.wearshare.activities.FileActivity;
import com.sau.wearshare.adapters.SendListAdapter;
import com.sau.wearshare.models.SendListViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 15-07-10.
 */
public class SendFragment extends Fragment implements WearableListView.ClickListener,
        WearableListView.OnLongClickListener  {

    private WearableListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_send, container, false);

        final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                listView = (WearableListView) view.findViewById(R.id.lst_items);
                listView.setGreedyTouchMode(true);
                setAdapter();
            }
        });


        return view;
    }

    private void setAdapter(){
        List<SendListViewItem> items = new ArrayList<>();

        items.add(new SendListViewItem("Click", R.drawable.ic_camera_white_24dp));
        items.add(new SendListViewItem("Folder", R.drawable.ic_folder_open_white_24dp));
        items.add(new SendListViewItem("File", R.drawable.ic_insert_drive_file_white_24dp));
        SendListAdapter adapter = new SendListAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        //listView.setLongClickable(true);
        listView.setClickListener(this);

        //listView.setOnLongClickListener(this);


    }



    @Override
    public boolean onLongClick(View view) {
        Toast.makeText(getActivity(), "Long", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        switch (viewHolder.getAdapterPosition()){
            case 0:
                startActivity(new Intent(getActivity(), ClickActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), FileActivity.class));
        }

    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
