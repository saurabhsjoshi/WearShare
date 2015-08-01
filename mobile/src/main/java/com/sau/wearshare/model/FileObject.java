package com.sau.wearshare.model;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by saurabh on 2015-08-01.
 */
public class FileObject {

    private String filename;
    private boolean isFile;

    public FileObject(String filename, boolean isFile){
        this.filename = filename;
        this.isFile = isFile;
    }

    public FileObject(DataMap map){
        this.filename = map.getString("filename");
        this.isFile = map.getBoolean("isFile");
    }

    public DataMap getDataMap(DataMap map){
        map.putString("filename", filename);
        map.putBoolean("isFile", isFile);
        return map;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isFile() {
        return isFile;
    }
}
