package com.sau.wearshare.fragments.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sau.wearshare.R;
import com.sau.wearshare.adapters.ExploreListAdapter;
import com.sau.wearshare.models.FileObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurabh on 2015-08-01.
 */
public class ExploreFragment extends Fragment implements WearableListView.ClickListener{

    private TextView txt_cur_path;
    private WearableListView lst_files;

    private ArrayList<String> full_path;

    private List<FileObject> files;
    private ExploreListAdapter exploreListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_explore, container, false);

        full_path = new ArrayList<>();
        full_path.add("home");

        txt_cur_path = (TextView) view.findViewById(R.id.txt_title);
        lst_files = (WearableListView) view.findViewById(R.id.lst_items);

        (view.findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        files = new ArrayList<>();

        /* TEMP CODE */
        for(int i = 0; i < 10; i++)
            files.add(new FileObject("file"+i,true));

        files.add(new FileObject("folder",false));
        files.add(new FileObject("file",true));
        files.add(new FileObject("file",true));

        for(int i =0 ; i < 25; i++)
            files.add(new FileObject("folder"+i, false));
        /* END TEMP */

        lst_files.setAdapter(new ExploreListAdapter(getActivity(), files));
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
}
