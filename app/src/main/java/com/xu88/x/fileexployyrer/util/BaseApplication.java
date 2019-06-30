package com.xu88.x.fileexployyrer.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.util.LinkedList;

import static android.content.ContentValues.TAG;

public class BaseApplication  extends Application{

        public static LinkedList<Activity> activityLinkedList;

        @Override
        public void onCreate() {
            super.onCreate();

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            activityLinkedList = new LinkedList<>();

            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    Log.d(TAG, "onActivityCreated: " + activity.getLocalClassName());
                    activityLinkedList.add(activity);
                    // 在Activity启动时（onCreate()） 写入Activity实例到容器内
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    Log.d(TAG, "onActivityDestroyed: " + activity.getLocalClassName());
                    activityLinkedList.remove(activity);
                    // 在Activity结束时（Destroyed（）） 写出Activity实例
                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

            });
        }

        public  void exitApp() {

            Log.d(TAG, "容器内的Activity列表如下 ");
            // 先打印当前容器内的Activity列表
            for (Activity activity : activityLinkedList) {
                Log.d(TAG, activity.getLocalClassName());
            }

            Log.d(TAG, "正逐步退出容器内所有Activity");

            // 逐个退出Activity
            for (Activity activity : activityLinkedList) {
                activity.finish();
            }

            //  结束进程
            // System.exit(0);
        }
}
