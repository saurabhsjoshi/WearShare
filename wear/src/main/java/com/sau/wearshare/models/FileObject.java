package com.sau.wearshare.models;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by saurabh on 2015-08-01.
 */
public class FileObject {

    private String filename;
    private String file_path;
    private boolean isFile;

    public FileObject(String filename, boolean isFile, String file_path){
        this.filename = filename;
        this.isFile = isFile;
        this.file_path = file_path;
    }

    public FileObject(DataMap map){
        this.filename = map.getString("filename");
        this.file_path = map.getString("file_path");
        this.isFile = map.getBoolean("isFile");

    }

    public DataMap getDataMap(DataMap map){
        map.putString("filename", filename);
        map.putString("file_path", file_path);
        map.putBoolean("isFile", isFile);
        return map;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilePath(){return  file_path;}

    public boolean isFile() {
        return isFile;
    }
}
