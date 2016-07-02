package com.example.ui;

import java.io.Serializable;

import download.http.entity.SimpleJsonReader;
import download.otherFileLoader.request.DownFile;

/**
 * Created by Stay on 18/8/15.
 * Powered by www.stay4it.com
 */
public class AppEntry extends SimpleJsonReader implements Serializable {
    public String name;
    public String icon;
    public String size;
    public String desc;
    public String url;
    public DownFile.DownloadStatus state = DownFile.DownloadStatus.IDLE;
    public int downLength;
    public int totalLength;


    @Override
    public String toString() {
        return name + "-----" + desc + "-----" + url;
    }

}
