package com.lee.downloaddemo.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lee.downloaddemo.db.ThreadDao;
import com.lee.downloaddemo.db.ThreadDaoImpl;
import com.lee.downloaddemo.entity.FileInfo;
import com.lee.downloaddemo.entity.ThreadInfo;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arvin.li on 2015/11/5.
 */
public class DownloadTask {

    private Context context;

    private FileInfo fileInfo;

    private ThreadDao mThreadDao;

    private int finished = 0;//下载完成进度

    public boolean isPause = false;//是否停止

    private List<ThreadInfo> threadList;//线程信息集合

    private List<DownloadThread> downloadThreadList;//下载线程集合

    private int threadNum;//文件分成几个线程下载

    public DownloadTask(Context context, FileInfo fileInfo, int threadNum) {
        this.context = context;
        this.fileInfo = fileInfo;
        this.threadNum = threadNum;

        mThreadDao = new ThreadDaoImpl(context);
    }

    //下载处理
    public void download() {

        finished = 0;
        isPause = false;

        //获取该URL的所有下载任务
        threadList = mThreadDao.queryThread(fileInfo.getUrl());

        Log.d("print", "fileInfo.getUrl()=" + fileInfo.getUrl());

        ThreadInfo threadInfo = null;

        if (threadList.size() == 0) {//第一次下载

            int perLength = fileInfo.getTotalLength() / threadNum;//每个线程下载长度

            //创建多个线程信息
            for (int i = 0; i < threadNum; i++) {

                int start = i * perLength;
                int end = (i + 1) * perLength - 1;

                threadInfo = new ThreadInfo(i, fileInfo.getUrl(), start, end, 0);

                //最后一个线程设置下载到最后，解决除不尽的问题
                if (i == threadNum - 1) {
                    threadInfo.setEnd(fileInfo.getTotalLength());
                }

                threadList.add(threadInfo);

                mThreadDao.insertThread(threadInfo);//把每个下载线程保存到数据库

            }
        }

        //启动所有线程下载
        if (threadList != null && threadList.size() > 0) {

            downloadThreadList = new ArrayList<>();

            for (ThreadInfo info : threadList) {

                finished += info.getFinished();//计算已经下载完成的长度

                DownloadThread downloadThread = new DownloadThread(info);
                downloadThread.start();

                downloadThreadList.add(downloadThread);
            }
        }
    }

    /**
     * 检测所有线程是否都下载完成，即文件是否下载完成
     * 加锁，同步任务，同一时间只有一个线程执行
     *
     * @return
     */
    private synchronized boolean checkAllThreadIsFinished() {

        if (downloadThreadList != null) {
            for (DownloadThread thread : downloadThreadList) {
                if (!thread.isFinished) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {

        private ThreadInfo threadInfo;

        public boolean isFinished = false;//线程是否下载完成

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            InputStream is = null;
            BufferedInputStream bis = null;

            //连接网络，下载
            try {
                URL url = new URL(threadInfo.getUrl());

                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setRequestMethod("GET");

                //设置下载位置
                int start = threadInfo.getStart() + threadInfo.getFinished();
                conn.addRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());

                if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {//部分下载响应成功

                    File saveFile = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());

                    RandomAccessFile raf = new RandomAccessFile(saveFile, "rwd");
                    //设置写入位置
                    raf.seek(start);

                    is = conn.getInputStream();
                    bis = new BufferedInputStream(is);

                    long lastTime = System.currentTimeMillis();

                    Intent intent = new Intent(DownloadService.ACTION_UPDATE);

                    int len;

                    byte bytes[] = new byte[1024 * 4];

                    while ((len = bis.read(bytes)) != -1) {

                        raf.write(bytes, 0, len);

                        finished += len;//累加该文件总下载进度

                        threadInfo.setFinished(threadInfo.getFinished() + len);//累加该线程下载进度

                        if (System.currentTimeMillis() - lastTime > 1000) {

                            int progress = finished * 100 / fileInfo.getTotalLength();

                            //1秒发送一次广播通知Activity更新下载进度
                            intent.putExtra("fileId", fileInfo.getId());
                            intent.putExtra("finished", progress);

                            context.sendBroadcast(intent);

                            lastTime = System.currentTimeMillis();
                        }

                        if (isPause) {//停止下载

                            mThreadDao.updateProgress(threadInfo.getId(), threadInfo.getUrl(), threadInfo.getFinished());//保存下载进度到数据库

                            return;
                        }
                    }

                    isFinished = true;//设置该线程已经下载完成
                    if (checkAllThreadIsFinished()) {//每个线程下载完成后都需要询问该文件是否下载完成
                        //下载完成，发送广播通知Activity
                        Intent intent1 = new Intent(DownloadService.DOWNLOAD_COMPLETED);
                        intent1.putExtra("fileId", fileInfo.getId());
                        intent1.putExtra("fileName", fileInfo.getFileName());
                        context.sendBroadcast(intent1);
                        //删除该文件的相关下载任务
                        mThreadDao.deleteThread(fileInfo.getUrl());
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    conn.disconnect();
                    is.close();
                    bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
