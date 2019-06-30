package com.xu88.x.fileexployyrer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.util.BaseApplication;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;
import com.xu88.x.fileexployyrer.util.SqliteHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity2 extends Activity {

    private String DB_PATH = null;
    private Integer DB_VERSION = null;

    private SqliteHelper xSettingsHelper = null;
    private SqliteHelper dbStatisticsHelper = null;

    private SharedPreferences userSettings = null;
    private static final String DB_VERSION_KEY = "db_version";

    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initUi();
        initDB();
        getDBVersion();
        getActiveInfo();
        registeBtn();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                ((BaseApplication)getApplication()).exitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initUi(){
        RuntimeUtil.transparentStatusBar(MainActivity2.this);
        userSettings = getSharedPreferences(getString(R.string.shared_preferences), 0);

        TextView title = findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity2.this, MainActivity3.class));
            }
        });
    }

    private void initDB(){
        DB_PATH = getString(R.string.db_path);
        DB_VERSION = Integer.valueOf(userSettings.getString(DB_VERSION_KEY, getString(R.string.db_version)));
        xSettingsHelper = new SqliteHelper(this, DB_PATH + "X_settings.db", DB_VERSION);
        dbStatisticsHelper = new SqliteHelper(this, DB_PATH + "db_statistics", DB_VERSION);
    }

    private void getDBVersion(){
        try {
            xSettingsHelper.query("select sqlite_version(*)");
        } catch (SQLiteException sex){
            try {
                String exception = sex.toString();
                if (exception.contains("Can't downgrade database from version")) {
                    DB_VERSION = Integer.valueOf(exception.substring(exception.indexOf("version ") + "version ".length(), exception.indexOf(" to")).trim());
                    userSettings.edit().putString(DB_VERSION_KEY, String.valueOf(DB_VERSION)).commit();
                    initDB();
                } else {
                    toast(exception);
                }
            } catch (Exception ex){
                toast("获取数据库版本失败！" + ex.toString());
            }
        }
    }

    private void getActiveInfo(){
        try {
            Cursor vip = xSettingsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "vip_tri"));
            if (0 == vip.getCount()) {
                findViewById(R.id.vip_cancel).setEnabled(Boolean.FALSE);
            } else {
                findViewById(R.id.vip_crack).setEnabled(Boolean.FALSE);
            }
        } catch (Exception ex){}

        try {
            Cursor screen = xSettingsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "screen_tri"));
            if (0 == screen.getCount()) {
                findViewById(R.id.regain_ads).setEnabled(Boolean.FALSE);
            } else {
                findViewById(R.id.ads_remove).setEnabled(Boolean.FALSE);
            }
        } catch (Exception ex){}

        try {
            Cursor trigger = dbStatisticsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "statistics_tri"));
            if (0 == trigger.getCount()) {
                findViewById(R.id.regain_statistics).setEnabled(Boolean.FALSE);
            } else {
                findViewById(R.id.stop_statistics).setEnabled(Boolean.FALSE);
            }
        } catch (Exception ex){}
    }

    private void registeBtn() {
        vip();
        ads();
        statistics();
    }

    private void vip() {

        Button vip_crack = findViewById(R.id.vip_crack);
        vip_crack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText et = new EditText(MainActivity2.this);
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                et.setText(String.valueOf(new Random().nextInt(334) + 31));

                new AlertDialog.Builder(MainActivity2.this)
                        .setTitle("请输入天数：")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    Integer days = Integer.valueOf(et.getText().toString());
                                    if (days < 1 || days > 365){
                                        toast("输入1 - 365的天数！");
                                        return;
                                    }

                                    Calendar instance = Calendar.getInstance();
                                    instance.setTime(new Date());
                                    instance.add(Calendar.DAY_OF_YEAR, days);

                                    String vip_sql = getString(R.string.vip_crack)
                                            .replace("{{date}}", DateFormat.format("yyyy-MM-dd", instance.getTime()))
                                            .replace("{{time}}", DateFormat.format("yyyy-MM-dd HH:mm:ss", instance.getTime()));
                                    xSettingsHelper.execSQL(vip_sql);
                                    Cursor cursor0 = xSettingsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "vip_tri"));
                                    toast(cursor0.getCount() == 1 ? ("升级会员成功!") : "升级会员失败！");
                                    if (cursor0.getCount() == 1){
                                        findViewById(R.id.vip_cancel).setEnabled(Boolean.TRUE);
                                        findViewById(R.id.vip_crack).setEnabled(Boolean.FALSE);
                                    }
                                } catch (NumberFormatException nfe){
                                    toast("请输入整数！");
                                } catch (SQLiteCantOpenDatabaseException scoe){
                                    toast("连接数据库失败！");
                                } catch (SQLiteException se){
                                   if (se.getMessage().contains("already exist")){
                                       toast("已是会员！");
                                       findViewById(R.id.vip_cancel).setEnabled(Boolean.TRUE);
                                       findViewById(R.id.vip_crack).setEnabled(Boolean.FALSE);
                                   } else {
                                       toast(se.toString());
                                   }
                                } catch (Exception ex){
                                    toast(ex.toString());
                                }
                            }
                        }).setNegativeButton("取消",null).show();
            }
        });

        Button vip_cancel = (Button) findViewById(R.id.vip_cancel);
        vip_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    xSettingsHelper.execSQL(getString(R.string.vip_cancel));
                    toast("去会员成功!");
                    findViewById(R.id.vip_cancel).setEnabled(Boolean.FALSE);
                    findViewById(R.id.vip_crack).setEnabled(Boolean.TRUE);
                } catch (SQLiteCantOpenDatabaseException scoe){
                    toast("连接数据库失败！");
                } catch (SQLiteException se){
                    if (se.getMessage().contains("no such")){
                        toast("已经取消了！");
                        findViewById(R.id.vip_cancel).setEnabled(Boolean.FALSE);
                        findViewById(R.id.vip_crack).setEnabled(Boolean.TRUE);
                    } else {
                        toast(se.toString());
                    }
                } catch (Exception ex){
                    toast(ex.getMessage());
                }
            }
        });
    }

    private void ads() {

        Button vip_crack = findViewById(R.id.ads_remove);
        vip_crack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                        String vip_sql = getString(R.string.remove_ads);
                        xSettingsHelper.execSQL(vip_sql);
                        Cursor cursor0 = xSettingsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "screen_tri"));
                        toast(cursor0.getCount() == 1 ? "去广告成功!" : "去广告失败！");
                        if (cursor0.getCount() == 1){
                            findViewById(R.id.ads_remove).setEnabled(Boolean.FALSE);
                            findViewById(R.id.regain_ads).setEnabled(Boolean.TRUE);
                        }
                    } catch (NumberFormatException nfe){
                        toast("请输入整数！");
                    } catch (SQLiteCantOpenDatabaseException scoe){
                        toast("连接数据库失败！");
                    } catch (SQLiteException se){
                        if (se.getMessage().contains("already exist")){
                            toast("已是屏蔽开屏广告！");
                            findViewById(R.id.ads_remove).setEnabled(Boolean.FALSE);
                            findViewById(R.id.regain_ads).setEnabled(Boolean.TRUE);
                        } else {
                            toast(se.toString());
                        }
                    } catch (Exception ex){
                        toast(ex.toString());
                    }
                }
        });

        Button vip_cancel = findViewById(R.id.regain_ads);
        vip_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    xSettingsHelper.execSQL(getString(R.string.regain_ads));
                    toast("开屏广告已恢复!");
                    findViewById(R.id.ads_remove).setEnabled(Boolean.TRUE);
                    findViewById(R.id.regain_ads).setEnabled(Boolean.FALSE);
                } catch (SQLiteCantOpenDatabaseException scoe){
                    toast("连接数据库失败！");
                } catch (SQLiteException se){
                    if (se.getMessage().contains("no such")){
                        toast("已经恢复了！");
                        findViewById(R.id.ads_remove).setEnabled(Boolean.TRUE);
                        findViewById(R.id.regain_ads).setEnabled(Boolean.FALSE);
                    } else {
                        toast(se.toString());
                    }
                } catch (Exception ex){
                    toast(ex.getMessage());
                }
            }
        });
    }

    private void statistics(){

        Button stop_statistics = (Button) findViewById(R.id.stop_statistics);
        stop_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dbStatisticsHelper.execSQL(getString(R.string.stop_statistics));
                    Cursor cursor = dbStatisticsHelper.query(getString(R.string.trigger_check).replace("{{trigger_name}}", "statistics_tri"));
                    toast(cursor.getCount() == 1 ? "屏蔽成功!" : "屏蔽失败！");
                    if (cursor.getCount() == 1){
                        findViewById(R.id.regain_statistics).setEnabled(Boolean.TRUE);
                        findViewById(R.id.stop_statistics).setEnabled(Boolean.FALSE);
                    }
                } catch (SQLiteCantOpenDatabaseException scoe){
                    toast("连接数据库失败！");
                } catch (SQLiteException se){
                    if (se.getMessage().contains("already exist")){
                        toast("已经屏蔽过了！");
                        findViewById(R.id.regain_statistics).setEnabled(Boolean.TRUE);
                        findViewById(R.id.stop_statistics).setEnabled(Boolean.FALSE);
                    } else {
                        toast(se.toString());
                    }
                } catch (Exception ex){
                    toast(ex.getMessage());
                }
            }
        });

        Button regain_statistics = findViewById(R.id.regain_statistics);
        regain_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dbStatisticsHelper.execSQL(getString(R.string.regain_statistics));
                    toast("恢复成功!");
                    findViewById(R.id.regain_statistics).setEnabled(Boolean.FALSE);
                    findViewById(R.id.stop_statistics).setEnabled(Boolean.TRUE);
                } catch (SQLiteCantOpenDatabaseException scoe){
                    toast("连接数据库失败！");
                } catch (SQLiteException se){
                    if (se.getMessage().contains("no such")){
                        toast("已经取消了！");
                        findViewById(R.id.regain_statistics).setEnabled(Boolean.FALSE);
                        findViewById(R.id.stop_statistics).setEnabled(Boolean.TRUE);
                    } else {
                        toast(se.toString());
                    }
                } catch (Exception ex){
                    toast(ex.getMessage());
                }
            }
        });
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
