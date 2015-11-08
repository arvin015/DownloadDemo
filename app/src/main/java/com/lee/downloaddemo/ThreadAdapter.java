package com.lee.downloaddemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lee.downloaddemo.entity.FileInfo;
import com.lee.downloaddemo.service.DownloadService;

import java.util.List;

/**
 * Created by arvin.li on 2015/11/6.
 */
public class ThreadAdapter extends BaseAdapter {

    private Context context;
    private List<FileInfo> fileList;
    private LayoutInflater inflater;

    public ThreadAdapter(Context context, List<FileInfo> fileList) {
        this.context = context;
        this.fileList = fileList;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int i) {
        return fileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final FileInfo fileInfo = fileList.get(i);

        ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = inflater.inflate(R.layout.list_item, null);

            viewHolder.nameText = (TextView) view.findViewById(R.id.nameText);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            viewHolder.downloadBtn = (ToggleButton) view.findViewById(R.id.downloadBtn);

            viewHolder.progressBar.setMax(100);
            viewHolder.nameText.setText(fileInfo.getFileName());
            viewHolder.downloadBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putExtra("file", fileInfo);

                    if (b) {
                        //开启后台Service开始下载
                        intent.setAction(DownloadService.ACTION_START);
                    } else {
                        //结束下载
                        intent.setAction(DownloadService.ACTION_STOP);
                    }

                    context.startService(intent);
                }
            });

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.progressBar.setProgress(fileInfo.getFinished());

        if (fileInfo.isFinished()) {
            viewHolder.downloadBtn.setEnabled(false);
            viewHolder.downloadBtn.setText("打开");
            viewHolder.progressBar.setProgress(0);
        }

        return view;
    }

    /**
     * 更新指定文件的下载进度
     *
     * @param fileId   下载文件ID
     * @param finished 下载完成长度
     */
    public void updateProgress(int fileId, int finished) {

        fileList.get(fileId - 1).setFinished(finished);

        notifyDataSetChanged();
    }

    /**
     * 设置指定文件已下载完成
     *
     * @param fileId
     */
    public void setCompleted(int fileId) {
        fileList.get(fileId - 1).setIsFinished(true);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView nameText;
        ProgressBar progressBar;
        ToggleButton downloadBtn;
    }
}
