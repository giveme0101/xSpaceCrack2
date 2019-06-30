package com.xu88.x.fileexployyrer.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.bean.ExpendLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExpendLogHelper extends SqliteHelper {

    private static Context contextHolder;
    private static ExpendLogHelper instance = null;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS expend_log (id INTEGER PRIMARY KEY AUTOINCREMENT, amount0 DOUBLE, year INTEGER, month INTEGER, day INTEGER, time VARCHAR)";
    private static final String DROP_TABLE = "DROP TABLE expend_log";

    private ExpendLogHelper(Context context, String name, int version) {
        super(context, name, version);
        this.contextHolder = context;
    }

    public static ExpendLogHelper getInstance(Context context){
        if (null != instance)
            return instance;

        String dataPath = RuntimeUtil.getAppFilePath();
        try {
            File path = new File(dataPath);
            if (!path.exists()) {
                path.mkdirs();
            }
        } catch (Exception ex){
            Toast.makeText(contextHolder, "创建数据文件夹失败，存放于APP Data目录！", Toast.LENGTH_LONG).show();
            dataPath = "";
        }
        instance = new ExpendLogHelper(context, dataPath + "expend_log.db", 2);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<ExpendLog> findAllList(){

        String sql = "  SELECT * FROM ( " +
                "            SELECT id, amount0, year, month, day, time FROM expend_log " +
                "          UNION " +
                "            SELECT -1 AS id, b.amount0 AS amount0, b.year AS year, b.month AS month, b.day AS day, '' AS time " +
                "            FROM ( " +
                "              SELECT  sum(amount0) AS amount0, year, month, count(*) * -1 AS day FROM expend_log " +
                "              GROUP by year, month  " +
                "            ) b  " +
                "        ) ORDER BY year DESC, month DESC, id DESC";
        List  result = new ArrayList<ExpendLog>();

        Cursor cursor = query(sql);
        while (cursor.moveToNext()) {
            ExpendLog log = new ExpendLog();
            log.setId(cursor.getInt(cursor.getColumnIndex("id")));
            log.setAmount0(cursor.getDouble(cursor.getColumnIndex("amount0")));
            log.setYear(cursor.getInt(cursor.getColumnIndex("year")));
            log.setMonth(cursor.getInt(cursor.getColumnIndex("month")));
            log.setDay(cursor.getInt(cursor.getColumnIndex("day")));
            log.setTime(cursor.getString(cursor.getColumnIndex("time")));
            result.add(log);
        }
        cursor.close();

        return result;
    }

    public int saveLog(ExpendLog log){
        if (null != log.getId()){
            ContentValues cv = new ContentValues();
            cv.put("amount0", log.getAmount0());
            return update("expend_log", cv, "id = ?", new String[]{String.valueOf(log.getId())});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("amount0", log.getAmount0());
            cv.put("year", new SimpleDateFormat("yyyy").format(new Date()));
            cv.put("month", new SimpleDateFormat("MM").format(new Date()));
            cv.put("day", new SimpleDateFormat("dd").format(new Date()));
            cv.put("time", new SimpleDateFormat("HH:mm:ss").format(new Date()));
            return (int) insert("expend_log", cv);
        }
    }

    public int deleteLog(ExpendLog log){
        return delete("expend_log","id = ?", new String[]{String.valueOf(log.getId())});
    }
}
