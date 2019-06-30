package com.xu88.x.fileexployyrer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xu88.x.fileexployyrer.R;
import com.xu88.x.fileexployyrer.bean.ExpendLog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class SqlListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private static DecimalFormat decimalFormatter = new DecimalFormat("0.00");

    private List<ExpendLog> logList = null;

    public SqlListAdapter(Context context, List<ExpendLog> logList) {

        this.logList = logList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return logList.size();
    }

    @Override
    public Object getItem(int position) {
        return logList.get(position).getId();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.expend_log, null);
            holder = new ViewHolder();
            holder.id = convertView.findViewById(R.id.id);
            holder.amount0 = convertView.findViewById(R.id.amount);
            holder.time = convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ExpendLog log = logList.get(position);
        holder.id.setText(String.valueOf(log.getId() < 1 ? "" : log.getId()));
        holder.amount0.setText(decimalFormatter.format((Double) log.getAmount0()));
        holder.time.setText(String.format("%d-%02d%s %s",
                                                log.getYear(),
                                                log.getMonth(),
                                                log.getDay() < 1 ? "" : String.format("-%02d",log.getDay()),
                                                log.getTime() == null || log.getTime().length() < 1 ? "统计" : log.getTime()));
        return convertView;
    }

    private class ViewHolder {
        private TextView id;
        private TextView amount0;
        private TextView amount1;
        private TextView time;
    }
}
