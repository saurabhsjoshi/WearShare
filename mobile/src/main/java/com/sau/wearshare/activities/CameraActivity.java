package com.sau.wearshare.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.sau.wearshare.R;
import com.sau.wearshare.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

/**
 * Created by saurabh on 2015-07-11.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback  {
    private static final String TAG = "CameraActivity";


    private static final String IMAGE_PATH = "/image_bitmap";
    private static final String IMAGE_KEY = "image_key";

    GoogleApiClient mGoogleApiClient;

    Context mContext;
    android.hardware.Camera cam;
    android.hardware.Camera.Parameters param;
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Logger.LOGD(TAG, "Shutter'd");
        }
    };

    Camera.PictureCallback jpgCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Logger.LOGD(TAG, "Picture taken");
            camera.stopPreview();
            camera.release();
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, null);
            new sendToWear().execute(bm);
        }
    };

    SurfaceView view;


    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        view = (SurfaceView) findViewById(R.id.surfaceView);
        mContext = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        view.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            cam = Camera.open();
            param = cam.getParameters();
            final Camera.Parameters params = cam.getParameters();
            final Camera.Size size = getOptimalSize();
            params.setPreviewSize(size.width, size.height);
            cam.setParameters(param);
            cam.setPreviewDisplay(view.getHolder());
            cam.startPreview();

            cam.takePicture(shutterCallback, null, jpgCallback);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void sendPhoto(Asset asset) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(IMAGE_PATH);
        dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private Camera.Size getOptimalSize() {
        Camera.Size result = null;
        final Camera.Parameters parameters = cam.getParameters();
        Display display = getWindowManager().getDefaultDisplay();
        Point winSize = new Point();
        display.getSize(winSize);
        int width = winSize.x;
        int height = winSize.y;
        for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width * 1.30 && size.height <= height * 1.30) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        if (result == null) {
            result = parameters.getSupportedPreviewSizes().get(0);
        }
        return result;
    }

    private void saveBitmapToFile(Bitmap bmp){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mContext.getFilesDir() + "/click.jpg");
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class sendToWear extends AsyncTask<Bitmap,Void,Void>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            view.setVisibility(View.INVISIBLE);
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("Sending data to your watch!");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            Bitmap bm = bitmaps[0];
            saveBitmapToFile(bm);
            //makeDummyFile();
            sendPhoto(toAsset(getResizedBitmap(bm, 500)));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(dialog.isShowing())
                dialog.dismiss();
            finish();
        }
    }

     private File makeDummyFile() {
         try {
             File file = File.createTempFile(mContext.getFilesDir() + "/click.jpg", null);
             file.deleteOnExit();

             OutputStream out = new BufferedOutputStream(
                     new FileOutputStream(file), 8192);
             Random generator = new Random();
             for(long i=0; i<700; i++) {
                 out.write(generator.nextInt());
             }
             out.close();
             return file;
         }catch (Exception ignore){}
         return null;
    }
}
