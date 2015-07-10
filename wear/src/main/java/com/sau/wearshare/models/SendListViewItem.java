package com.sau.wearshare.models;

/**
 * Created by saurabh on 15-07-09.
 */
public class SendListViewItem {
    public int iconRes;
    public String title;

    public SendListViewItem(String title, int iconRes){
        this.title = title;
        this.iconRes = iconRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }
}
