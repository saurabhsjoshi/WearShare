package com.sau.wearshare.fragments.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sau.wearshare.R;
import com.sau.wearshare.adapters.ExploreListAdapter;
import com.sau.wearshare.models.DataHolder;
import com.sau.wearshare.models.FileObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 2015-08-01.
 */
public class SelectionFragment extends Fragment {
    private List<FileObject> files;
    private WearableListView lst_files;
    private ExploreListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_selection, container, false);
        files = new ArrayList<>();
        lst_files = (WearableListView) view.findViewById(R.id.lst_items);
        adapter = new ExploreListAdapter(getActivity(), files);
        lst_files.setAdapter(adapter);
        lst_files.setGreedyTouchMode(true);
        updateList();
        return view;
    }

    public void updateList(){
        files.add(DataHolder.selectedItem);
        adapter.notifyDataSetChanged();

    }
}