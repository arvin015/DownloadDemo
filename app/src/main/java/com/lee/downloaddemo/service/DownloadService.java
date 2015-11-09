package com.lee.downloaddemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.lee.downloaddemo.db.FileDao;
import com.lee.downloaddemo.db.FileDaoImpl;
import com.lee.downloaddemo.entity.FileInfo;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 下载Service
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public class DownloadService extends Service {

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/downloads";

    public static final String ACTION_START = "ACTION_START";            //开始下载
    public static final String ACTION_STOP = "ACTION_STOP";              //停止下载
    public static final String ACTION_UPDATE = "ACTION_UPDATE";          //更新下载进度
    public static final String ACTION_QUIT = "ACTION_QUIT";              //退出应用
    public static final String ACTION_STOP_ALL = "ACTION_STOP_ALL";      //停止所有下载
    public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";//下载完成

    private static final int GET_SUCCESS = 10000;

    private FileDao mFileDao = new FileDaoImpl(this);

    private Map<Integer, DownloadTask> downloadTasks = new LinkedHashMap<>();//文件下载任务哈希集合

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == GET_SUCCESS) {
                //设置文件总长度
                FileInfo fileInfo = (FileInfo) msg.obj;

                //开始下载
                DownloadTask downloadTask = new DownloadTask(DownloadService.this, fileInfo, 3);
                downloadTask.download();

                downloadTasks.put(fileInfo.getId(), downloadTask);
            }
        }
    };

    /**
     * 接收Activity传递的参数
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (ACTION_QUIT.equals(action)) {

            stopAllTask(true);

        } else if (ACTION_STOP_ALL.equals(action)) {

            stopAllTask(false);

        } else {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("file");

            DownloadTask currentTask = null;

            if (downloadTasks.containsKey(fileInfo.getId())) {
                currentTask = downloadTasks.get(fileInfo.getId());
            }

            if (ACTION_START.equals(action)) {//开始下载

                if (currentTask == null) {
                    new MyThread(fileInfo).start();//启动初始化线程，获取文件长度，并在本地创建相应大小的保存文件
                } else {
                    currentTask.download();
                }

            } else if (ACTION_STOP.equals(action)) {//停止下载

                if (currentTask != null) {
                    currentTask.isPause = true;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 停止所有的下载任务
     *
     * @param needUpdateFile 是否需要保存下载文件信息到本地
     */
    public void stopAllTask(boolean needUpdateFile) {
        for (Integer key : downloadTasks.keySet()) {
            DownloadTask downloadTask = downloadTasks.get(key);

            if (needUpdateFile) {
                mFileDao.updateFile(downloadTask.fileInfo);
            }

            downloadTask.isPause = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化线程---获取文件长度，并在本地创建相应大小的保存文件
     */
    class MyThread extends Thread {

        private FileInfo fileInfo;

        public MyThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;

            try {

                //连接网络
                URL url = new URL(fileInfo.getUrl());

                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setRequestMethod("GET");

                int length = 0;

                //响应成功
                if (conn.getResponseCode() == HttpStatus.SC_OK) {

                    //获取文件总长度
                    length = conn.getContentLength();
                }

                if (length <= 0) {
                    return;
                }

                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File saveFile = new File(dir,
                        fileInfo.getUrl().substring(fileInfo.getUrl().lastIndexOf("/") + 1));

                //创建可任意写入的本地保存文件，并设置长度
                raf = new RandomAccessFile(saveFile, "rwd");
                raf.setLength(length);

                fileInfo.setTotalLength(length);

                Message msg = Message.obtain();
                msg.what = GET_SUCCESS;
                msg.obj = fileInfo;
                handler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
