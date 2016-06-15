package com.example.ui;

import java.io.Serializable;

import download.http.entity.SimpleJsonReader;

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

    @Override
    public String toString() {
        return name + "-----" + desc + "-----" + url;
    }

}
