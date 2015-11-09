package com.lee.downloaddemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lee.downloaddemo.db.FileDao;
import com.lee.downloaddemo.db.FileDaoImpl;
import com.lee.downloaddemo.db.ThreadDaoImpl;
import com.lee.downloaddemo.entity.FileInfo;
import com.lee.downloaddemo.service.DownloadService;

import java.io.File;
import java.util.List;

/**
 * Created by arvin.li on 2015/11/6.
 */
public class ThreadAdapter extends BaseAdapter {

    private Context context;
    private List<FileInfo> fileList;
    private LayoutInflater inflater;

    private boolean isReset = false;//是否是重设

    private FileInfo currentDeleteFile;//当前需要删除的文件

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

        final ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = inflater.inflate(R.layout.list_item, null);

            viewHolder.nameText = (TextView) view.findViewById(R.id.nameText);
            viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            viewHolder.downloadBtn = (ToggleButton) view.findViewById(R.id.downloadBtn);
            viewHolder.deleteBtn = (Button) view.findViewById(R.id.deleteBtn);

            viewHolder.progressBar.setMax(100);
            viewHolder.nameText.setText(fileInfo.getFileName());
            viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putExtra("file", fileInfo);

                    if (viewHolder.downloadBtn.isChecked()) {
                        //开启后台Service开始下载
                        intent.setAction(DownloadService.ACTION_START);

                        //保存文件信息到数据库
                        FileDao mFileDao = new FileDaoImpl(context);
                        mFileDao.insertFile(fileInfo);

                        isReset = false;
                        currentDeleteFile = null;

                    } else {
                        //结束下载
                        intent.setAction(DownloadService.ACTION_STOP);
                    }

                    context.startService(intent);
                }
            });

            viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("确定删除该下载文件？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    currentDeleteFile = fileInfo;

                                    //停止该文件下载
                                    Intent intent = new Intent(context, DownloadService.class);
                                    intent.putExtra("file", fileInfo);
                                    intent.setAction(DownloadService.ACTION_STOP);
                                    context.startService(intent);

                                    //删除本地该下载文件及本地数据库下载任务信息
                                    new ThreadDaoImpl(context).deleteThread(fileInfo.getUrl());//删除数据库下载任务信息
                                    new FileDaoImpl(context).deleteFileByFileId(fileInfo.getId());//删除数据库文件信息

                                    File file = new File(DownloadService.DOWNLOAD_PATH,
                                            fileInfo.getUrl().substring(fileInfo.getUrl().lastIndexOf("/") + 1));
                                    if (file.exists()) {
                                        file.delete();
                                    }

                                    //重设文件信息
                                    fileInfo.setFinished(0);
                                    fileInfo.setIsFinished(false);

                                    viewHolder.deleteBtn.setVisibility(View.GONE);
                                    viewHolder.downloadBtn.setEnabled(true);
                                    viewHolder.downloadBtn.setChecked(false);
                                    viewHolder.progressBar.setProgress(0);

                                }
                            })
                            .setNegativeButton("取消", null).show();
                }
            });

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.progressBar.setProgress(fileInfo.getFinished());

        if (fileInfo.getFinished() > 0) {//显示删除按钮
            viewHolder.deleteBtn.setVisibility(View.VISIBLE);
        }

        if (fileInfo.isFinished()) {
            viewHolder.downloadBtn.setEnabled(false);
            viewHolder.downloadBtn.setText("下载完成");
        } else {
            viewHolder.downloadBtn.setEnabled(true);
        }

        if (isReset) {//删除全部文件，全部按钮重设
            viewHolder.downloadBtn.setChecked(false);
            viewHolder.deleteBtn.setVisibility(View.GONE);
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


        //当前更新是当前需删除的文件或者删除所有文件，则不执行更新
        if ((currentDeleteFile != null && fileId == currentDeleteFile.getId())
                || isReset) {
            return;
        }

        fileList.get(fileId - 1).setFinished(finished);

        notifyDataSetChanged();
    }

    /**
     * 设置指定文件已下载完成
     *
     * @param fileInfo
     */
    public void setCompleted(FileInfo fileInfo) {
        fileList.get(fileInfo.getId() - 1).setIsFinished(fileInfo.isFinished());
        fileList.get(fileInfo.getId() - 1).setFinished(fileInfo.getFinished());
        notifyDataSetChanged();
    }

    /**
     * 重设文件列表
     */
    public void resetFileList() {

        for (FileInfo fileInfo : fileList) {
            fileInfo.setFinished(0);
            fileInfo.setIsFinished(false);
        }

        isReset = true;

        notifyDataSetChanged();

    }

    static class ViewHolder {
        TextView nameText;
        ProgressBar progressBar;
        ToggleButton downloadBtn;
        Button deleteBtn;
    }
}
