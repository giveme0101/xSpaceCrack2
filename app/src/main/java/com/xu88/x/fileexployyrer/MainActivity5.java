package com.xu88.x.fileexployyrer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.util.FileUtil;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;

import java.io.File;

public class MainActivity5 extends AppCompatActivity {

    private String uri = null;
    private String contextCache = null;
    private Boolean isChange = Boolean.FALSE;
    private Integer init = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init = 0;
        uri = this.getIntent().getDataString();

        final View fab = findViewById(R.id.fab);
        final EditText editText = findViewById(R.id.textEditor);
        loadContext(editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
               if (init++ != 0){
                   isChange = Boolean.TRUE;
                   fab.setVisibility(View.VISIBLE);
               }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String context = editText.getText().toString();
                    FileUtil.writeFile(null, getFilePath(), context, Boolean.FALSE);
                    isChange = Boolean.FALSE;
                    findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                    Snackbar.make(view, "保存成功！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception ex){
                    RuntimeUtil.toast(MainActivity5.this, ex.getMessage());
                }
            }
        });
    }

    private String getFilePath(){
        if (uri.startsWith("xu88")){
            return uri.replace("xu88://file.handle.text?path=", "");
        }

        return uri.replace("file://", "");
    }

    private void loadContext(final EditText editText){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contextCache = FileUtil.getFile(null, getFilePath());
                        editText.setText(contextCache);
                        TextView textView = findViewById(R.id.toolbar_title);
                        textView.setText(new File(getFilePath()).getName());
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final EditText editText = findViewById(R.id.textEditor);
            if (isChange){
                new AlertDialog.Builder(MainActivity5.this)
                        .setTitle("确认保存!")
                        .setMessage("文档已修改，是否保存并关闭？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String context = editText.getText().toString();
                                    FileUtil.writeFile(null, getFilePath(), context, Boolean.FALSE);
                                    RuntimeUtil.toast(MainActivity5.this, "保存成功！");
                                    finish();
                                } catch (Exception ex){
                                    RuntimeUtil.toast(MainActivity5.this, ex.getMessage());
                                }
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).show();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
