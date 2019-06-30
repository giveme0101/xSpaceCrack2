package com.xu88.x.fileexployyrer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu88.x.fileexployyrer.R;
import com.xu88.x.fileexployyrer.util.FileUtil;
import com.xu88.x.fileexployyrer.util.RuntimeUtil;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Bitmap directory, file;

    //存储文件名称
    private File current = null;
    private List<File> fileList = null;

    //参数初始化
    public ListAdapter(Context context, File current, List<File> fileList) {

        this.fileList = fileList;
        this.current = current;

        directory = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
        file = BitmapFactory.decodeResource(context.getResources(), R.drawable.file);

        //缩小图片
        directory = small(directory, 0.1f);
        file = small(file, 0.1f);

        inflater = LayoutInflater.from(context);
    }

    public void sortFileList(){
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o, File t1) {
                if (o.isDirectory() && t1.isFile()) return -1;
                if (o.isFile() && t1.isDirectory()) return 1;

                return o.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
            }
        });
    }

    public void setFileList(List<File> fileList){
        this.fileList = fileList;
    }

    public void setFile(File file){
        this.current = file;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.file, null);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.textView);
            holder.image = convertView.findViewById(R.id.imageView);
            holder.itemsNum = convertView.findViewById(R.id.info);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        File f = new File(fileList.get(position).getPath().toString());
        holder.text.setText(f.getName());
        if (f.isDirectory()) {
            holder.image.setImageBitmap(directory);
        } else if (f.isFile()) {
            holder.image.setImageBitmap(file);
        }
        holder.itemsNum.setText(getInfo(f));

        return convertView;
    }

    private String getInfo(File file){
        if (file.isDirectory()){
            return String.valueOf(file.listFiles().length) + " items";
        } else {
            return FileUtil.getFileSize(file);
        }
    }

    private class ViewHolder {
        private TextView text;
        private ImageView image;
        private TextView itemsNum;
    }

    private Bitmap small(Bitmap map, float num) {
        Matrix matrix = new Matrix();
        matrix.postScale(num, num);
        return Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), matrix, true);
    }
}
