package com.sau.wearshare.fragments.click;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sau.wearshare.R;
import com.sau.wearshare.activities.ClickActivity;

/**
 * Created by saurabh on 2015-07-11.
 */
public class ClickPhotoFragment extends Fragment implements
        DelayedConfirmationView.DelayedConfirmationListener{

    private CircularButton btn_click;
    private TextView lbl_click;
    private DelayedConfirmationView mDelayedView;
    private LinearLayout ll_delay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_click_photo, container, false);

        btn_click = (CircularButton) view.findViewById(R.id.btn_click);
        ll_delay = (LinearLayout) view.findViewById(R.id.ll_confirm);
        lbl_click = (TextView) view.findViewById(R.id.lbl_click);
        mDelayedView =
                (DelayedConfirmationView) view.findViewById(R.id.delayed_confirm);
        mDelayedView.setListener(this);


        btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideControls();
                mDelayedView.setTotalTimeMs(4000);
                mDelayedView.start();

            }
        });

        btn_click.setColor(getResources().getColor(R.color.btn_click_red));
        return view;
    }

    private void hideControls(){
        btn_click.setVisibility(View.INVISIBLE);
        lbl_click.setVisibility(View.INVISIBLE);
        ll_delay.setVisibility(View.VISIBLE);
    }

    private void showControls(){
        btn_click.setVisibility(View.VISIBLE);
        lbl_click.setVisibility(View.VISIBLE);
        ll_delay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTimerFinished(View view) {
        showControls();
        ((ClickActivity) getActivity()).addFragments();
    }

    @Override
    public void onTimerSelected(View view) {
        mDelayedView.reset();
        showControls();
    }
}
