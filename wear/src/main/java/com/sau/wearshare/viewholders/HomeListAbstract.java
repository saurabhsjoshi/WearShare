package com.sau.wearshare.viewholders;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.widget.FrameLayout;

/**
 * Created by saurabh on 15-07-09.
 */
public abstract class HomeListAbstract extends FrameLayout implements WearableListView.OnCenterProximityListener  {

    public HomeListAbstract(Context context){
        super(context);
    }
    @Override
    public abstract void onCenterPosition(boolean b);

    @Override
    public abstract void onNonCenterPosition(boolean b);
}
