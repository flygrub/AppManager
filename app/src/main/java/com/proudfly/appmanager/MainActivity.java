package com.proudfly.appmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //单例
    public static MainActivity singleton;

    private static final String TAG  = "MainActivity";

    private List<ApplicationInfo> killAppList;

    private ActivityManager am;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        singleton = this;

        if(upgradeRootPermission(getPackageCodePath()))
        {
            Toast.makeText(MainActivity.this, "已经获取Root权限", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(MainActivity.this, "未获取Root权限", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    //初始化
    private void init()
    {
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //获取包管理器，在这里主要通过包名获取程序的图标和程序名
        pm =this.getPackageManager();
        initListView(1);
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
            AlertDialog("正在努力开发中！");
            return true;
        }
        else if (id == R.id.action_getTirdRuningList)
        {
            initListView(1);
            return true;
        }
        else if (id == R.id.action_getOwnRuningList)
        {
            initListView(0);
            return true;
        }
        else if (id == R.id.action_killTirdApp)
        {
            killTridApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*
     * 获取应用列表
     */
    private void initListView(int type)
    {
        List<Programe> list = getRunningProcess(type);
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
    public List<Programe> getRunningProcess(int type){

        Log.d(TAG, "getRunningProcess --- start -----");

        PackagesInfo pi = new PackagesInfo(this);

        //获取正在运行的应用
        List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
        List<Programe> list = new ArrayList<Programe>();

        Log.d(TAG, "getRunningProcess --- Start --- For Info -----");

        for(ActivityManager.RunningAppProcessInfo ra : run)
        {
            ApplicationInfo info =  pi.getInfo(ra.processName);
            if(info != null)
            {
                //屏蔽掉自己
                if(info.packageName.equals(getPackageName()))
                {
                    continue;
                }

                //判断是否为系统预装的应用
                if (type == 0 && (info.flags & info.FLAG_SYSTEM) <= 0)
                {
                    //屏蔽第三方
                    continue;
                }
                if(type == 1 && (info.flags & info.FLAG_SYSTEM) > 0)
                {
                    //屏蔽系统应用
                    continue;
                }

                Programe pr = new Programe();
                pr.setIcon(info.loadIcon(pm));
                pr.setName(info.loadLabel(pm).toString());
                pr.setInfo(info.packageName);
                list.add(pr);

                //保存第三方应用列表
                if(type == 1)
                {
                    if (killAppList == null)
                        killAppList = new ArrayList<ApplicationInfo>();
                    killAppList.add(info);
                }

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

    private void killTridApp()
    {
//        AlertDialog("正在努力开发中！");
        if(killAppList != null)
        {
            for(ApplicationInfo ai : killAppList)
            {
                //魅族定制，防止清理联系人应用
//                if(!ai.packageName.contains("meizu") && ai.packageName.contains(""))
//                {
                    Log.d(TAG, "killTridApp --- " + ai.packageName);
                    try
                    {
//                        am.killBackgroundProcesses(ai.packageName);
//                        forceStopPackage(ai.packageName);
                        KillUtil.kill(ai.packageName);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }


//                }
            }
        }
    }

    /**
     *强制停止应用程序
     * @param pkgName
     */
    private void forceStopPackage(String pkgName) throws Exception{
        Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
        method.invoke(am, pkgName);
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void AlertDialog(String m)
    {
        new  AlertDialog.Builder(this)
                .setTitle("提示" )
                .setMessage(m)
                .setPositiveButton("确定" ,  null )
                .show();
    }
}
