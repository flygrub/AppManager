package com.proudfly.appmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //单例
    public static MainActivity singleton;

    private static final String TAG  = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        singleton = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    //初始化
    private void init()
    {
        initListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_getRuningList)
        {
            initListView();
        }

        return super.onOptionsItemSelected(item);
    }
    /*
     * 获取应用列表
     */
    private void initListView()
    {
        List<Programe> list = getRunningProcess();
        ListAdapter adapter = new ListAdapter(list, getApplicationContext());
        getListView().setAdapter(adapter);
    }

    /*
     * 获取ListView
     */
    private ListView getListView()
    {
        return (ListView) findViewById(R.id.myArrayList);
    }

    //正在运行的
    public List<Programe> getRunningProcess(){

        Log.d(TAG, "getRunningProcess --- start -----");

        PackagesInfo pi = new PackagesInfo(this);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //获取正在运行的应用
        List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
        //获取包管理器，在这里主要通过包名获取程序的图标和程序名
        PackageManager pm =this.getPackageManager();
        List<Programe> list = new ArrayList<Programe>();

        Log.d(TAG, "getRunningProcess --- Start --- For Info -----");
        for(ActivityManager.RunningAppProcessInfo ra : run){
            //过滤系统的应用和电话应用
//            if(ra.processName.equals("system") || ra.processName.equals("com.android.phone")){
//                continue;
//            }


            ApplicationInfo info =  pi.getInfo(ra.processName);
            if(info != null) {
                Programe pr = new Programe();
                pr.setIcon(info.loadIcon(pm));
                pr.setName(info.loadLabel(pm).toString());
                pr.setInfo(info.packageName);
                list.add(pr);
                Log.d(TAG, "ApplicationInfo == -----" + info.toString());
            }
            else
            {
                Log.d(TAG, "ApplicationInfo == ----- null");
            }
        }
        Log.d(TAG, "getRunningProcess --- End -----");
        return list;
    }
}
