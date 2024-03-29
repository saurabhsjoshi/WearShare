package com.sau.wearshare.viewholders;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sau.wearshare.R;

/**
 * Created by saurabh on 15-07-09.
 */
public class HomeListItemView extends FrameLayout implements WearableListView.OnCenterProximityListener{

    final CircledImageView image;
    final TextView text;

    public HomeListItemView(Context context){
        super(context);
        View.inflate(context, R.layout.listitem_send, this);
        image = (CircledImageView) findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);
    }

    @Override
    public void onCenterPosition(boolean b) {
        //Animation example to be ran when the view becomes the centered one
        image.animate().scaleX(1f).scaleY(1f).alpha(1);
        text.animate().scaleX(1f).scaleY(1f).alpha(1);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
    //Animation example to be ran when the view is not the centered one anymore
        image.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        text.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
    }
}
