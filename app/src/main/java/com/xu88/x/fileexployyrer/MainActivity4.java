package com.xu88.x.fileexployyrer;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.bean.SqlConnectionRecord;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;
import com.xu88.x.fileexployyrer.util.XMLUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbfile_resolver);

        if (!RuntimeUtil.isRunInVirtual()){
            Toast.makeText(this, "仅支持在分身环境运行！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String xmlPath = RuntimeUtil.getVirtualAppPath() +"/org.cl.sql/files/record.xml";

        String uri = this.getIntent().getDataString();
        if (uri != null && uri.length() < 1){
            finish();
            return;
        }

        if (uri.startsWith("xu88")){
            addToSqlEditor(xmlPath, uri.replace("xu88://file.handle.sqlite?path=", ""));
        } else {
            addToSqlEditor(xmlPath, uri.replace("file://", ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void addToSqlEditor(String xmlPath, String filePath){
        try {
            File file = new File(filePath);
            Toast.makeText(this, file.exists() + file.getPath() , Toast.LENGTH_LONG).show();

            List<SqlConnectionRecord> records = readXML(xmlPath);
            Boolean isExist = Boolean.FALSE;
            for (SqlConnectionRecord record : records){
                if (file.getName().equals(record.getName())){
                    isExist = Boolean.TRUE;
                    break;
                }
            }

            if (!isExist){
                SqlConnectionRecord record = new SqlConnectionRecord();
                record.setName(file.getName());
                record.setUserName("");
                record.setHostName(file.getPath().replace("file:", ""));
                record.setPortName("null");
                record.setServiceName("null");
                record.setPassword("");
                record.setConnectType("0");
                records.add(record);
                XMLUtil.writeToXml(records, "record", "connectName", xmlPath);
            }
            Toast.makeText(this, "添加成功！", Toast.LENGTH_LONG).show();
            startSqlEditor();
        } catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startSqlEditor(){
        try {
            ComponentName componentName = new ComponentName("org.cl.sql", "org.cl.sql.MainActivity");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "启动SQL编辑器失败！", Toast.LENGTH_LONG).show();
        }
    }

    private List<SqlConnectionRecord> readXML(String path){
        InputStream is = null;
        List<SqlConnectionRecord> records = Collections.emptyList();
        try {
           is = new FileInputStream(new File(path));
            List <String > fields =new ArrayList<>();
            fields.add("userName");
            fields.add("hostName");
            fields.add("portName");
            fields.add("serviceName");
            fields.add("password");
            fields.add("connectType");
            records = XMLUtil.parse(is, SqlConnectionRecord.class, fields, fields, "connectName");
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return records;
    }
}
