package com.proudfly.appmanager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by LW on 2016/2/18.
 */
public class ListViewControll {

    public static ListViewControll singleton;

    private ListView listView;



    public ListViewControll()
    {
        Init();
    }

    private void Init()
    {
        singleton = this;
        listView = (ListView) MainActivity.singleton.findViewById(R.id.myArrayList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                OnItemClick(view);
            }
        });
    }

    /*
     * 获取ListView
     */
    public ListView getListView()
    {
        return listView;
    }

    private void OnItemClick(View view)
    {
        final View viewTemp = view;
        TextView textInfo = (TextView)view.findViewById(R.id.info);
        final String packgeStr = textInfo.getText().toString();

        TextView textName = (TextView)view.findViewById(R.id.title);
        String nameStr = textName.getText().toString();

        if(!AppDataModel.singleton.appIgnorList.containsKey(packgeStr))
            AppDataModel.singleton.appIgnorList.put(packgeStr, false);

        final String msgStr;
        if(!AppDataModel.singleton.appIgnorList.get(packgeStr))
        {
            msgStr = "添加 " + nameStr + " 到白名单";
        }
        else
        {
            msgStr = "将 " + nameStr + " 从白名单删除";
        }
        MainActivity.AlertDialog_OKAndCancel(msgStr, new DialogInterface.OnClickListener() {
            //String _msgStr = msgStr;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Boolean bo = !AppDataModel.singleton.appIgnorList.get(packgeStr);
                AppDataModel.singleton.appIgnorList.remove(packgeStr);
                AppDataModel.singleton.appIgnorList.put(packgeStr, bo);

                Toast.makeText(MainActivity.singleton, "已" + msgStr, Toast.LENGTH_SHORT).show();

                Log.d("sss", "bo = " + bo);
                if(bo)
                {
                    viewTemp.setBackgroundColor(Color.parseColor("#185e14"));
                }
                else
                {
                    viewTemp.setBackgroundColor(Color.parseColor("#000000"));
                }
                Log.d("sss", "viewTemp = " + viewTemp.getSolidColor());
            }
        });
    }

    public void setAdapter(ListAdapter adapter)
    {
        listView.setAdapter(adapter);
    }
}
