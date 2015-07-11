package com.sau.wearshare.fragments.click;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircularButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sau.wearshare.R;
import com.sau.wearshare.activities.ClickActivity;

/**
 * Created by saurabh on 2015-07-11.
 */
public class SendPhotoFragment extends Fragment {
    CircularButton btn_talk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_send_photo, container, false);
        btn_talk = (CircularButton) view.findViewById(R.id.btn_send);
        btn_talk.setColor(getResources().getColor(R.color.btn_talk_blue));
        btn_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ClickActivity)getActivity()).sendPicturePath();
            }
        });


        return view;
    }
}
