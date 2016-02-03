package com.proudfly.appmanager;

import android.graphics.drawable.Drawable;

/**
 * Created by LW on 2016/2/3.
 * 应用信息
 */
public class Programe {
    //图标
    private Drawable icon;
    //程序名
    private String name;
    //程序信息
    private String info;

    public Drawable getIcon() {
        return icon;
    }
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getInfo(){
        return info;
    }
    public void setInfo(String info){
        this.info = info;
    }
}
