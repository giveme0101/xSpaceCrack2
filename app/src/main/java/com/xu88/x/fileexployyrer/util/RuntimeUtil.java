package com.xu88.x.fileexployyrer.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.MainActivity;
import com.xu88.x.fileexployyrer.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class RuntimeUtil {

    private static Boolean RUN_VIRTUAL = null;
    public static Integer APP_COUNTER = 1;

    public static final String X_SPACE_DATA_PATH = "/data/user/0/com.godinsec.godinsec_private_space";
    public static final String G_SPACE_DATA_PATH = "/data/user/0/com.excean.gspace";

    private static String VIRTUAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getPath();
    private static String STORAGE_PATH = Environment.getExternalStorageDirectory().getPath();

    private static Context applicationContext = null;

    public static String getRootPath(){
        if (!RuntimeUtil.isRunInVirtual()){
            return STORAGE_PATH;
        }

        if (isXSpace()){
            switch (APP_COUNTER % 3){
                case 0:
                    return getDataPath();
                case 1:
                    return getVirtualStoragePath();
                case 2:
                default:
                    return getStoragePath();
            }
        }

        switch (APP_COUNTER % 2){
            case 0:
                return getDataPath();
            case 2:
            default:
                return getStoragePath();
        }
    }

    public static String getAppFilePath (){
        return RuntimeUtil.getStoragePath() + "/xu88/";
    }

    public static String getVirtualStoragePath(){
        if (!RuntimeUtil.isRunInVirtual())
            return STORAGE_PATH;

        return VIRTUAL_STORAGE_PATH;
    }

    public static String getStoragePath(){
        if (!RuntimeUtil.isRunInVirtual())
            return STORAGE_PATH;

        if (isXSpace())
            return VIRTUAL_STORAGE_PATH.replace("/godinsec", "");

        return STORAGE_PATH;
    }

    public static String getDataPath(){
        String data_path = applicationContext.getDataDir().getPath();
        if (data_path.contains(X_SPACE_DATA_PATH))
            return X_SPACE_DATA_PATH;

        if (data_path.contains(G_SPACE_DATA_PATH))
            return G_SPACE_DATA_PATH;

        return data_path;
    }

    public static String getVirtualAppPath(){
        if (!RuntimeUtil.isRunInVirtual())
            return null;

        if (isXSpace())
            return getDataPath () + "/app_VApps";

        if (isGSpace())
            return getDataPath() + "/gameplugins";

        return null;
    }

    public static Boolean isXSpace(){
        String data_path = applicationContext.getDataDir().getPath();
        return data_path.contains(X_SPACE_DATA_PATH);
    }

    public static Boolean isGSpace(){
        String data_path = applicationContext.getDataDir().getPath();
        return  data_path.contains(G_SPACE_DATA_PATH);
    }

    public static void setApplicationContext(Context context){
        applicationContext = context;
    }

    public static String getTitle(){
        if (!RuntimeUtil.isRunInVirtual()){
            return "File Explorer";
        }

        if (isXSpace()) {
            switch (APP_COUNTER % 3) {
                case 0:
                    return "Data Explorer";
                case 1:
                    return "Virtual File Explorer";
                case 2:
                default:
                    return "File Explorer";
            }
        }

        switch (APP_COUNTER % 2) {
            case 0:
                return "Data Explorer";
            case 2:
            default:
                return "File Explorer";
        }
    }

    public static Boolean isDataPath(){
        return getDataPath().equals(getRootPath());
    }

    public static void toast(Context context, String title){
        Toast.makeText(context, title, Toast.LENGTH_LONG).show();
    }

    public static void dialog(Activity activity, String title, String[] menu, final DialogOkHandle okHandle){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                okHandle.ok(dialog, which);
            }
        };
        TextView titleView = new TextView(activity);
        titleView.setText(title);
        titleView.setPadding(10, 20, 10, 10);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextSize(20);
        new AlertDialog.Builder(activity)
                .setCustomTitle(titleView)
                .setItems(menu, listener)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    public static interface DialogOkHandle{
        void ok(DialogInterface dialog, int which);
    }

    /**
     * 动态的设置状态栏  实现沉浸式状态栏
     */
    public static void transparentStatusBar(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            LinearLayout linear_bar = (LinearLayout) activity.findViewById(R.id.ll_bar);
            linear_bar.setVisibility(View.VISIBLE);
            //获取到状态栏的高度
            int statusHeight = getStatusBarHeight(activity);
            //动态的设置隐藏布局的高度
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_bar.getLayoutParams();
            params.height = statusHeight;
            linear_bar.setLayoutParams(params);
        }
    }
    /**
     * 通过反射的方式获取状态栏高度
     * @return
     */
    private static int getStatusBarHeight(Activity activity) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isRunInVirtual() {

        if  (null == RUN_VIRTUAL)
            RUN_VIRTUAL = checkVirtual() || isGSpace() || isXSpace() ;

        return RUN_VIRTUAL;
    }

    public static Boolean checkVirtual(){
        String filter = getUidStrFormat();
        String result = exec("ps");
        if (result == null || result.isEmpty()) {
            return false;
        }

        String[] lines = result.split("\n");
        if (lines == null || lines.length <= 0) {
            return false;
        }

        int exitDirCount = 0;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(filter)) {
                int pkgStartIndex = lines[i].lastIndexOf(" ");
                String processName = lines[i].substring(pkgStartIndex <= 0
                        ? 0 : pkgStartIndex + 1, lines[i].length());
                File dataFile = new File(String.format("/data/data/%s",
                        processName, Locale.CHINA));
                if (dataFile.exists()) {
                    exitDirCount++;
                }
            }
        }

        return exitDirCount > 1;
    }

    private static String exec(String command) {
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

            bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedOutputStream.write(command.getBytes());
            bufferedOutputStream.write('\n');
            bufferedOutputStream.flush();
            bufferedOutputStream.close();

            process.waitFor();

            String outputStr = getStrFromBufferInputSteam(bufferedInputStream);
            return outputStr;
        } catch (Exception e) {
            return null;
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static String getStrFromBufferInputSteam(BufferedInputStream bufferedInputStream) {
        if (null == bufferedInputStream) {
            return "";
        }
        int BUFFER_SIZE = 512;
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder result = new StringBuilder();
        try {
            while (true) {
                int read = bufferedInputStream.read(buffer);
                if (read > 0) {
                    result.append(new String(buffer, 0, read));
                }
                if (read < BUFFER_SIZE) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String getUidStrFormat() {
        String filter = exec("cat /proc/self/cgroup");
        if (filter == null || filter.length() == 0) {
            return null;
        }

        int uidStartIndex = filter.lastIndexOf("uid");
        int uidEndIndex = filter.lastIndexOf("/pid");
        if (uidStartIndex < 0) {
            return null;
        }
        if (uidEndIndex <= 0) {
            uidEndIndex = filter.length();
        }

        filter = filter.substring(uidStartIndex + 4, uidEndIndex);
        try {
            String strUid = filter.replaceAll("\n", "");
            if (isNumber(strUid)) {
                int uid = Integer.valueOf(strUid);
                filter = String.format("u0_a%d", uid - 10000);
                return filter;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isNumber(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void requirePermission(Activity activity, AfterPermission chain){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                chain.hasPermission();
            }
        }
    }

    public static void notifyPermission(Activity activity, NotifyPermission notifyPermission, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (0 != grantResults[i]){
                    Toast.makeText(activity, "请赋予权限后再试！", Toast.LENGTH_LONG).show();
                    activity.finish();
                } else {
                    notifyPermission.notifyPermission();
                }
            }
        }
    }

    public interface AfterPermission{
        void hasPermission();
    }

    public interface NotifyPermission{
        void notifyPermission();
    }
}