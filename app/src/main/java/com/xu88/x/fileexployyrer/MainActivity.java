package com.xu88.x.fileexployyrer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xu88.x.fileexployyrer.adapter.ListAdapter;
import com.xu88.x.fileexployyrer.util.FileUtil;
import com.xu88.x.fileexployyrer.util.MimeTypeUtil;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {

    private List<File> fileList = new ArrayList<>();
    private ListAdapter listAdapter = null;

    private File currentFile = null;
    private File currentPath = null;

    private List<Map<String, Object>> pathBtns;
    private TextView[] pathViews;

    private long mExitTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        RuntimeUtil.setApplicationContext(getApplicationContext());
        RuntimeUtil.transparentStatusBar(MainActivity.this);
        RuntimeUtil.requirePermission(this, new RuntimeUtil.AfterPermission() {
            @Override
            public void hasPermission() {
                register();
                refreshUI(RuntimeUtil.getRootPath());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimeUtil.notifyPermission(this, new RuntimeUtil.NotifyPermission() {
            @Override
            public void notifyPermission() {
                register();
                refreshUI(RuntimeUtil.getRootPath());
            }
        }, requestCode, permissions, grantResults);
    }

    private void register(){
        final ImageButton add = findViewById(R.id.top_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (new File(currentPath.getPath() + File.separator + currentFile.getName()).exists()){
                        displayToast("目标已存在！");
                    } else {
                        FileUtil.copy(currentFile, new File(currentPath.getPath() + File.separator));
                        File newFile = new File(currentPath.getPath() + File.separator, currentFile.getName());
                        if (newFile.exists()){
                            displayToast("复制成功！");
                            fileList.add(newFile);
                            listAdapter.sortFileList();
                            listAdapter.notifyDataSetChanged();
                            currentFile = null;
                            add.setVisibility(View.INVISIBLE );
                        } else {
                            displayToast("黏贴失败！");
                        }
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                    displayToast(ex.getMessage());
                }
            }
        });

        ListView lv = getListView();
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean  onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File file = fileList.get(position);
                if (file.isDirectory()){
                    folderHandle(file, position);
                }
                return false;
            }
        });

        TextView title = findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RuntimeUtil.APP_COUNTER++;
                refreshUI(RuntimeUtil.getRootPath());
            }
        });
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity3.class));
                return false;
            }
        });

        registPathBtns();
    }

    private void registPathBtns(){
        pathBtns = new ArrayList<Map<String, Object>>(3){{
            add(new HashMap<String, Object>(2){{
                put("btn", findViewById(R.id.path_btn0));
                put("file", null);
            }});
            add(new HashMap<String, Object>(2){{
                put("btn", findViewById(R.id.path_btn1));
                put("file", null);
            }});
            add(new HashMap<String, Object>(2){{
                put("btn", findViewById(R.id.path_btn2));
                put("file", null);
            }});
        }};
        pathViews = new TextView[]{findViewById(R.id.path_txt0), findViewById(R.id.path_txt1)};
        for (int i = 0; i < pathBtns.size(); i++){
            final Map item = pathBtns.get(i);
            ((Button)item.get("btn")).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentPath = (File) item.get("file");
                    refreshUI(currentPath.getPath());
                }
            });
        }
    }

    private void refreshUI(String path){
        refreshFileList(path == null ? RuntimeUtil.getRootPath() : path);
        refreshPathBtns();
        refreshTitle();
    }

    private void refreshPathBtns(){

        Deque<File> pfiles = new ArrayDeque<>(pathBtns.size());
        File temp = currentPath;
        for (int i = 0; i < pathBtns.size(); i++){
            pfiles.addFirst(temp);
            if (RuntimeUtil.getRootPath().equals(temp.getPath())){
                break;
            }

            temp = temp.getParentFile();
        }

        int parentLength = pfiles.size();

        for (int i = 0; i < pathBtns.size(); i++){
            File file = pfiles.peekFirst();
            if (null == file){
                pathBtns.get(i).put("file", null);
            } else {
                pathBtns.get(i).put("file", pfiles.removeFirst());
            }
        }

        for (int i = 0, j = pathBtns.size(); i < j; i++){
            Map item = pathBtns.get(i);
            Button button = (Button) item.get("btn");
            File file = (File) item.get("file");
            if (null != file){
                if (RuntimeUtil.getRootPath().equals(file.getPath())){
                    button.setText("/");
                } else {
                    button.setText(file.getName());
                }
                button .setVisibility(View.VISIBLE);
            } else {
                ((Button) item.get("btn")).setVisibility(View.INVISIBLE);
            }
        }

        for (int i = 0; i < pathViews.length; i++){
            if (i < parentLength - 1){
                pathViews[i].setVisibility(View.VISIBLE);
            } else {
                pathViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void refreshTitle(){
        TextView title = findViewById(R.id.title);
        title.setText(RuntimeUtil.getTitle());
    }

    private void folderHandle(final File file, final int position) {
       RuntimeUtil.dialog(this, "请选择要进行的操作", new String[]{"重命名", "复制文件夹", "删除文件夹"}, new RuntimeUtil.DialogOkHandle() {
            @Override
            public void ok(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        renameFile(file, position);
                        break;
                    case 1:
                        currentFile = file;
                        displayToast("文件已复制到剪贴板！");
                        findViewById(R.id.top_add).setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        deleteFolder(file, position);
                        break;
                    case 3:
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try{
                if (RuntimeUtil.getRootPath().equals(currentPath.getPath())){
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        finish();
                        System.exit(0);
                    }
                } else {
                    currentPath = currentPath.getParentFile();
                    refreshUI(currentPath.getPath());
                }
            } catch (Exception ex){
                ex.printStackTrace();
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 扫描显示文件列表
     * @param path
     */
    private void refreshFileList(final String path) {
        fileList.clear();
        final File file = new File(path);

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isHidden();
            }
        });

        currentPath = file;
        if (null == files) return;
        for (final File f : files) {
            fileList.add(f);
        }

        if (null == listAdapter){
            listAdapter = new ListAdapter(this, file, fileList);
        } else {
            listAdapter.setFile(file);
            listAdapter.setFileList(fileList);
        }

        listAdapter.sortFileList();
        this.setListAdapter(listAdapter);
    }

    /**
     * 点击事件
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String path = fileList.get(position).getPath();
        File file = new File(path);

        if (file.exists() && file.canRead()) {
            if (file.isDirectory()) {
                refreshUI(path);
            } else {
                fileHandle(file, position);
            }
        } else {
            Resources res = getResources();
            new AlertDialog.Builder(this).setTitle("Message")
                    .setMessage(res.getString(R.string.no_permission))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
        super.onListItemClick(l, v, position, id);
    }

    private void fileHandle(final File file, final int position) {
        RuntimeUtil.dialog(this, "请选择要进行的操作", new String[]{"打开文件", "打开方式", "重命名", "复制文件", "删除文件", "复制路径"}, new RuntimeUtil.DialogOkHandle() {
            @Override
            public void ok(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        openFile(file);
                        break;
                    case 1:
                        openFileSelectType(file);
                        break;
                    case 2:
                        renameFile(file, position);
                        break;
                    case 3:
                        currentFile = file;
                        displayToast("文件已复制到剪贴板！");
                        findViewById(R.id.top_add).setVisibility(View.VISIBLE );
                        break;
                    case 4:
                        deleteFile(file, position);
                        break;
                    case 5:
                        copyPath(file);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void openFileWithType(File file, String type) {
        try {
            final Uri uri = Uri.fromFile(file);
            if ("database/sqlite".equals(type)){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("xu88://file.handle.sqlite?path=" + uri.getPath())));
                return;
            }
            if ("text/plain".equals(type)){
                RuntimeUtil.dialog(this, "文本文件", new String[]{"应用内打开", "其他程序打开"}, new RuntimeUtil.DialogOkHandle() {
                    @Override
                    public void ok(DialogInterface dialog, int which) {
                        if (0 == which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("xu88://file.handle.text?path=" + uri.getPath())));
                            } catch (Exception ex){
                                RuntimeUtil.toast(MainActivity.this, ex.getMessage());
                            }
                        } else {
                            fileHandle(uri, "text/plain");
                        }
                    }
                });

                return;
            }

            fileHandle(uri, type);
        } catch (Exception ex){
            displayToast(ex.getMessage());
        }
    }

    private void fileHandle(Uri uri, String type){
        Intent intent = new Intent();
        intent.setDataAndType(uri, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);
    }

    private void openFile(final File file) {
        String type = MimeTypeUtil.getMIMEType(file.getPath());
        if (!"*/*".equals(type)) {
            openFileWithType(file, type);
            return;
        }

        openFileSelectType(file);
    }

    private void openFileSelectType(final File file){
        RuntimeUtil.dialog(this, "打开方式", new String[]{"文本文件", "音频文件", "视频文件", "图片文件", "SQLite数据库", "其他" }, new RuntimeUtil.DialogOkHandle() {
            @Override
            public void ok(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        openFileWithType(file, "text/plain");
                        break;
                    case 1:
                        openFileWithType(file, "audio/*");
                        break;
                    case 2:
                        openFileWithType(file, "video/*");
                        break;
                    case 3:
                        openFileWithType(file, "image/*");
                        break;
                    case 4:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("xu88://file.handle.sqlite?path=" + file.getPath())));
                        break;
                    case 5:
                        openFileWithType(file, "*/*");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void deleteFolder(final File file, final int position){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("注意!")
                .setMessage("确定要删除此文件夹吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (FileUtil.deleteFolder(file)) {
                            displayToast("删除成功！");
                            fileList.remove(position);
                            listAdapter.notifyDataSetChanged();
                        } else {
                            displayToast("删除失败！");
                        }
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    private void deleteFile(final File file, final int position){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("注意!")
                .setMessage("确定要删除此文件吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()) {
                            displayToast("删除成功！");
                            fileList.remove(position);
                            listAdapter.notifyDataSetChanged();
                        } else {
                            displayToast("删除失败！");
                        }
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    private void copyPath(File file){

        ClipData  myClip = ClipData.newPlainText("text", file.getPath());
        ClipboardManager myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(getApplicationContext(), "File path Copied!", Toast.LENGTH_SHORT).show();
    }

    private void renameFile(final File file, final int position){

        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        View view = factory.inflate(R.layout.rename_dialog, null);
        final EditText editText =  view.findViewById(R.id.editText);
        editText.setText(file.getName());

        DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String modifyName = editText.getText().toString();
                final String fpath = file.getParentFile().getPath();
                final File newFile = new File(fpath + "/" + modifyName);
                if (newFile.exists()) {
                    if (!modifyName.equals(file.getName())) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("注意!")
                                .setMessage("文件名已存在，是否覆盖？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (file.renameTo(newFile)) {
                                            fileList.set(position, newFile);
                                            listAdapter.sortFileList();
                                            listAdapter.notifyDataSetChanged();
                                            displayToast("重命名成功！");
                                        } else {
                                            displayToast("重命名失败！");
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } else {
                    if (file.renameTo(newFile)) {
                        fileList.set(position, newFile);
                        listAdapter.sortFileList();
                        listAdapter.notifyDataSetChanged();
                        displayToast("重命名成功！");
                    } else {
                        displayToast("重命名失败！");
                    }
                }
            }
        };
        AlertDialog renameDialog = new AlertDialog.Builder(MainActivity.this).create();
        renameDialog.setView(view);
        renameDialog.setButton("确定", listener2);
        renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        renameDialog.show();
    }

    private void displayToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
