package com.sau.wearshare.adapters;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sau.wearshare.R;
import com.sau.wearshare.models.FileObject;
import com.sau.wearshare.viewholders.HomeListItemView;

import java.util.List;

/**
 * Created by saurabh on 2015-08-01.
 */
public class ExploreListAdapter extends WearableListView.Adapter {
    private final Context context;
    private final List<FileObject> items;

    public ExploreListAdapter(Context context, List<FileObject> items){
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
        final FileObject item = items.get(position);

        ((TextView) view.findViewById(R.id.text)).setText(item.getFilename());
        if(item.isFile())
            ((CircledImageView) view.findViewById(R.id.image)).setImageDrawable(context.getDrawable(R.drawable.ic_insert_drive_file_white_24dp));
        else
            ((CircledImageView) view.findViewById(R.id.image)).setImageDrawable(context.getDrawable(R.drawable.ic_folder_open_white_24dp));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
