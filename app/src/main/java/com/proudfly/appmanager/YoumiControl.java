package com.proudfly.appmanager;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import kll.dod.rtk.AdManager;
import kll.dod.rtk.br.AdSize;
import kll.dod.rtk.br.AdView;
import kll.dod.rtk.br.AdViewListener;

/**
 * Created by LW on 2016/2/22.
 */
public class YoumiControl {

    public String TAG = "YoumiControl";

    public YoumiControl()
    {
        Init();
    }

    private void Init()
    {
        AdManager.getInstance(MainActivity.singleton).init(AppDataModel.youmiID, AppDataModel.youmiKey, false);

        setupBannerAd();
    }

    /**
     * 设置广告条广告
     */
    private void setupBannerAd()
    {
        //　实例化广告条
//        AdView adView = new AdView(MainActivity.singleton, AdSize.SIZE_320x50);
//        LinearLayout bannerLayout = (LinearLayout) MainActivity.singleton.findViewById(R.id.youmi_banner);
//        bannerLayout.addView(adView);
//        Log.d("Youmi", "setupBannerAd end");

        /**
         * 悬浮布局
         */
        // 实例化LayoutParams(重要)
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        //　设置广告条的悬浮位置，这里示例为右下角
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        //　实例化广告条
        AdView adView = new AdView(MainActivity.singleton, AdSize.FIT_SCREEN);
        // 监听广告条接口
        adView.setAdListener(new AdViewListener() {

            @Override
            public void onSwitchedAd(AdView arg0) {
                Log.i(TAG, "广告条切换");
            }

            @Override
            public void onReceivedAd(AdView arg0) {
                Log.i(TAG, "请求广告成功");
            }

            @Override
            public void onFailedToReceivedAd(AdView arg0) {
                Log.i(TAG, "请求广告失败");
            }
        });
        // 调用Activity的addContentView函数
        ((Activity) MainActivity.singleton).addContentView(adView, layoutParams);
    }
}
