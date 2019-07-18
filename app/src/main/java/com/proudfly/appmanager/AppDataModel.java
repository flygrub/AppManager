package com.proudfly.appmanager;

import android.content.Entity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.DropBoxManager;
import android.support.annotation.NonNull;
import android.text.LoginFilter;
import android.util.Log;

import java.security.KeyStore;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by LW on 2016/2/18.
 */
public class AppDataModel {
    public static AppDataModel singleton = new AppDataModel();

    public static final String TAG = "AppDataModel";

    public Map<String, Boolean> appIgnorList;
    public List<ApplicationInfo> TrirdAppList;
    public List<ApplicationInfo> SelfAppList;

    private static final String PREFS_NAME = "AM_ignore";

    private static final String saveListID = "ignoreList";

    public static final String youmiID = "50ef42f24b9c8396";
    public static final String youmiKey = "f263d55e516b3fba";

    public AppDataModel()
    {
        Init();
    }

    private void Init()
    {
        appIgnorList = new HashMap<String, Boolean>();
        TrirdAppList = new ArrayList<ApplicationInfo>();
        SelfAppList = new ArrayList<ApplicationInfo>();
    }

    public void saveIgnoreList() {
        if(appIgnorList.size() > 0)
        {
            Log.d(TAG, "saveIgnoreList --- start");
            SharedPreferences settings = MainActivity.singleton.getSharedPreferences(PREFS_NAME, MainActivity.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(saveListID, ignoreListToStr());
            editor.commit();
            Log.d(TAG, "saveIgnoreList ---- end");
        }
    }

    /*
    从本地获取白名单列表
     */
    public void getIgnoreList()
    {
        SharedPreferences settings = MainActivity.singleton.getSharedPreferences(PREFS_NAME, MainActivity.MODE_PRIVATE);
        String ignoreListStr = settings.getString(saveListID, "");
        Log.d(TAG, "ignoreListStr = &" + ignoreListStr + "&");
        //本地保存有数据
        if(!ignoreListStr.equals(""))
        {
            //先清空当前的列表
            appIgnorList.clear();
            //如果只有一条数据直接加入列表
            if(ignoreListStr.contains("&"))
            {
                String[] strTemp = null;
                strTemp = ignoreListStr.split("&");
                int size = strTemp.length;
                for(int i =0;i<size;i++)
                {
                    appIgnorList.put(strTemp[i], true);
                    Log.d(TAG, "SharedPreferences --i--" + i + "--" + strTemp[i]);
                }
            }
            else
                appIgnorList.put(ignoreListStr, true);
        }
    }

    private String ignoreListToStr()
    {
        String list = "";
        for (Map.Entry<String, Boolean> entry: appIgnorList.entrySet()) {
            if(entry.getValue())
            {
                list += entry.getKey() + "&";
            }
        }

        list = list.substring(0, list.length() - 1);
        Log.d(TAG, "ignoreList = " + list);
        return list;
    }

}
