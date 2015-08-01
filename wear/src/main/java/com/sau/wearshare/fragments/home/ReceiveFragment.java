package com.sau.wearshare.fragments.home;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sau.wearshare.Home;
import com.sau.wearshare.R;

/**
 * Created by saurabh on 15-07-10.
 */
public class ReceiveFragment extends Fragment {
    CircularButton btn_talk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_receive, container, false);

        final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                btn_talk = (CircularButton) view.findViewById(R.id.btn_talk);
                btn_talk.setColor(getResources().getColor(R.color.btn_talk_blue));
                btn_talk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Home)getActivity()).displaySpeechRecognizer();
                    }
                });
            }
        });

        return view;
    }

    private void setAdapter(){
    }
}
