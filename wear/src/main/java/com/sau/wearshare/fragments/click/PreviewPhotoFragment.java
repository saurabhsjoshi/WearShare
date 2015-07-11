package com.sau.wearshare.fragments.click;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sau.wearshare.R;

/**
 * Created by saurabh on 2015-07-11.
 */
public class PreviewPhotoFragment extends Fragment {
    private ImageView mPhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_photo, container, false);
        mPhoto = (ImageView) view.findViewById(R.id.photo);
        return view;
    }

    public void setBackgroundImage(Bitmap bitmap) {
        if(bitmap != null)
            mPhoto.setImageBitmap(bitmap);
    }
}
