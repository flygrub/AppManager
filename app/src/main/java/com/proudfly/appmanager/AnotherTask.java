package com.proudfly.appmanager;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by LW on 2016/2/19.
 */
public class AnotherTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "AnotherTask";

    @Override
    protected void onPostExecute(String result) {

        //对UI组件的更新操作
        MainActivity.singleton.initListView(1);
    }
    @Override
    protected String doInBackground(String... params) {
        //耗时的操作
        return params[0];
    }
}
