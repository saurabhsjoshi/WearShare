package com.sau.wearshare.adapters;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sau.wearshare.R;
import com.sau.wearshare.models.SendListViewItem;
import com.sau.wearshare.viewholders.HomeListItemView;

import java.util.List;

/**
 * Created by saurabh on 15-07-09.
 */
public class SendListAdapter extends WearableListView.Adapter {
    private final Context context;
    private final List<SendListViewItem> items;


    public SendListAdapter(Context context, List<SendListViewItem> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(new HomeListItemView(context));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        HomeListItemView view = (HomeListItemView) holder.itemView;
        final SendListViewItem item = items.get(position);

        ((TextView) view.findViewById(R.id.text)).setText(item.getTitle());
        ((ImageView) view.findViewById(R.id.image)).setImageResource(item.getIconRes());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
