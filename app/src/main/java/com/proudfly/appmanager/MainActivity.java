package com.proudfly.appmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
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

    private ActivityManager am;
    private PackageManager pm;

    private  int nullRuningAppCount = 0;

    private YoumiControl youmi;

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

        AppDataModel.singleton.getIgnoreList();

        init();

        youmi = new YoumiControl();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestory--");
        AppDataModel.singleton.saveIgnoreList();
        super.onDestroy();
    }

    //初始化
    private void init()
    {
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //获取包管理器，在这里主要通过包名获取程序的图标和程序名
        pm =this.getPackageManager();

        ListViewControll.singleton = new ListViewControll();

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
            AlertDialog("点击应用列表可以添加应用到白名单。\n注意：本应用只针对第三方应用进行管理！更多功能请联系作者。\n\n邮箱：flygrub@126.com.");
            return true;
        }
        else if (id == R.id.action_getTirdRuningList)
        {
            initListView(1);
            return true;
        }
//        else if (id == R.id.action_getOwnRuningList)
//        {
//            initListView(0);
//            return true;
//        }
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
    public void initListView(int type)
    {
        List<Programe> list = getRunningProcess(type);
        ListAdapter adapter = new ListAdapter(list, getApplicationContext());
        ListViewControll.singleton.setAdapter(adapter);

    }

    //正在运行的
    public List<Programe> getRunningProcess(int type){

        Log.d(TAG, "getRunningProcess --- start -----");
        AppDataModel.singleton.TrirdAppList.clear();
        AppDataModel.singleton.SelfAppList.clear();
        nullRuningAppCount = 0;

        PackagesInfo pi = new PackagesInfo(MainActivity.singleton);

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
                if ((info.flags & info.FLAG_SYSTEM) <= 0)
                {
                    AppDataModel.singleton.TrirdAppList.add(info);
                    if(type == 0)//屏蔽第三方
                        continue;
                }
                if((info.flags & info.FLAG_SYSTEM) > 0)
                {
                    AppDataModel.singleton.SelfAppList.add(info);
                    if(type == 1)//屏蔽系统应用
                        continue;
                }

                Programe pr = new Programe();
                pr.setIcon(info.loadIcon(pm));
                pr.setName(info.loadLabel(pm).toString());
                pr.setInfo(info.packageName);
                list.add(pr);

                Log.d(TAG, "ApplicationInfo == -----" + info.toString());
            }
            else
            {
                nullRuningAppCount++;
                Log.d(TAG, "ApplicationInfo == ----- null");
            }
        }
        Log.d(TAG, "getRunningProcess --- End -----");
        Log.d(TAG, "run.size() = " + run.size());
        Log.d(TAG, "AppDataModel.singleton.SelfAppList.size() = " + AppDataModel.singleton.SelfAppList.size());
        Log.d(TAG, "AppDataModel.singleton.TrirdAppList.size() = " + AppDataModel.singleton.TrirdAppList.size());
        Log.d(TAG, "nullRuningAppCount = " + nullRuningAppCount);
        return list;
    }

    private void killTridApp()
    {
//        AlertDialog("正在努力开发中！");
        if(AppDataModel.singleton.TrirdAppList != null)
        {
//            String[] cmd = new String[AppDataModel.singleton.TrirdAppList.size()];
            int i =0;
            for(ApplicationInfo ai : AppDataModel.singleton.TrirdAppList)
            {
                //魅族定制，防止清理联系人应用
                if(!AppDataModel.singleton.appIgnorList.containsKey(ai.packageName) || !AppDataModel.singleton.appIgnorList.get(ai.packageName))
                {
                    Log.d(TAG, "killTridApp --- " + ai.packageName);
//                    cmd[i] = "am force-stop " + ai.packageName + " \n";
                    try
                    {
//                        am.killBackgroundProcesses(ai.packageName);
//                        forceStopPackage(ai.packageName);
                        KillUtil.kill(ai.packageName);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        break;
                    }

                }
                i++;
            }

//            try {
//
//                ShellCommand.execCommand(cmd, true, new ShellCommand.ShellCommandListener() {
//                    @Override
//                    public void onCommandFinished(ShellCommand.CommandResult result) {
//                        Log.d(TAG, result.toString());
//                        onCommandFinish(result);
//                    }
//                });
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//                AlertDialog("您权限不够，不能结束应用，确保设备以获取Root权限！");
//            }

            Log.d(TAG, "killTridApp == ----- End");

            new Thread() {
                @Override
                public void run() {
                    while (true)
                    {
                        try {
                            sleep(500);

                            Log.d("Thread Run", "getThridAppCount = " + getThridAppCount() + "AppDataModel.singleton.appIgnorList.size() = " + AppDataModel.singleton.appIgnorList.size());
                            if(getThridAppCount() <= AppDataModel.singleton.appIgnorList.size())
                            {
                                new AnotherTask().execute("JSON1");
                                break;
                            }
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }
            }.start();
        }
    }

    private int currentKillIndex = 0;
    private void onCommandFinish(ShellCommand.CommandResult result)
    {
        currentKillIndex++;
        if(currentKillIndex >= AppDataModel.singleton.TrirdAppList.size() - AppDataModel.singleton.appIgnorList.size())
        {
            currentKillIndex = 0;
            new AnotherTask().execute("JSON1");
        }
    }
    private int getThridAppCount()
    {
        List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
        int count = 0;//run.size() - nullRuningAppCount - AppDataModel.singleton.SelfAppList.size();




        PackagesInfo pi = new PackagesInfo(MainActivity.singleton);
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
                if ((info.flags & info.FLAG_SYSTEM) <= 0)
                {
                    count++;
                    continue;
                }
                if((info.flags & info.FLAG_SYSTEM) > 0)
                {

                    continue;
                }
            }
        }




        Log.d("Thread Run", "run.size() = " + run.size() + "AppDataModel.singleton.SelfAppList.size() = " + AppDataModel.singleton.SelfAppList.size());
        return count;
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

    public static void AlertDialog(String m)
    {
        new  AlertDialog.Builder(MainActivity.singleton)
                .setTitle("提示" )
                .setMessage(m)
                .setPositiveButton("确定" ,  null )
                .show();
    }
    public static void AlertDialog_OKAndCancel(String m, DialogInterface.OnClickListener listener)
    {
        new  AlertDialog.Builder(MainActivity.singleton)
                .setTitle("提示" )
                .setMessage(m)
                .setPositiveButton("确定" ,  listener )
                .setNegativeButton("取消", null)
                .show();
    }
}
