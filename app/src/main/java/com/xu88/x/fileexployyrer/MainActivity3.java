package com.xu88.x.fileexployyrer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.adapter.SqlListAdapter;
import com.xu88.x.fileexployyrer.bean.ExpendLog;
import com.xu88.x.fileexployyrer.util.ExpendLogHelper;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends ListActivity {

    ExpendLogHelper helper = null;
    private List<ExpendLog> expendLogs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        init();
        register();
        refreshUI();
    }

    private void init(){
        RuntimeUtil.transparentStatusBar(MainActivity3.this);
        helper = ExpendLogHelper.getInstance(this);
    }

    private void register(){
        findViewById(R.id.top_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(null);
            }
        });
        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RuntimeUtil.isXSpace())
                    startActivity(new Intent(MainActivity3.this, MainActivity2.class));
            }
        });
    }

    private void refreshUI(){
        refreshList();
    }

    private void handle(final ExpendLog log) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        save(log);
                        break;
                    case 1:
                        delete(log);
                        break;
                    default:
                        break;
                }
            }
        };
        String[] menu = {"修改", "删除"};
        new AlertDialog.Builder(MainActivity3.this)
                .setTitle("请选择要进行的操作!")
                .setItems(menu, listener)
                .setPositiveButton("取消", null).show();
    }

    private void delete(ExpendLog log){
        try {
            int row = helper.deleteLog(log);
            displayToast(row > 0 ? "操作成功！" : "操作失败！");
            refreshUI();
        } catch (Exception ex){
            displayToast(ex.getMessage());
        }
    }

    private void save(final ExpendLog log){
        LayoutInflater factory = LayoutInflater.from(MainActivity3.this);
        View view = factory.inflate(R.layout.expend_log_save, null);
        final EditText editText =  view.findViewById(R.id.amount0);
        editText.setText(null != log ? String.valueOf(log.getAmount0()) : "");

        AlertDialog renameDialog = new AlertDialog.Builder(MainActivity3.this).create();
        renameDialog.setView(view);
        renameDialog.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final Double amount0 = Double.valueOf(editText.getText().toString());
                    int row = helper.saveLog(new ExpendLog() {{
                        setId(null != log ? log.getId() : null);
                        setAmount0(amount0);
                    }});
                    displayToast(row > 0 ? "操作成功！" : "操作失败！");
                    refreshUI();
                } catch (Exception ex){
                    displayToast(ex.getMessage());
                }
            }
        });
        renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        renameDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshList() {
        expendLogs.clear();

        List<ExpendLog> list = helper.findAllList();

        if (null == list) return;
        for (final ExpendLog log : list) {
            expendLogs.add(log);
        }

        this.setListAdapter(new SqlListAdapter(this, expendLogs));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ExpendLog log = expendLogs.get(position);
        handle(log);
        super.onListItemClick(l, v, position, id);
    }

    private void displayToast(String message) {
        Toast.makeText(MainActivity3.this, message, Toast.LENGTH_LONG).show();
    }
}
