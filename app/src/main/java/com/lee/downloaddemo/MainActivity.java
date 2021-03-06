package com.lee.downloaddemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lee.downloaddemo.db.FileDao;
import com.lee.downloaddemo.db.FileDaoImpl;
import com.lee.downloaddemo.db.ThreadDaoImpl;
import com.lee.downloaddemo.entity.FileInfo;
import com.lee.downloaddemo.service.DownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private Context context;

    private ListView threadListView;
    private Button clearBtn;

    private MyBroadcast mBroadcast;

    private List<FileInfo> fileList;

    private ThreadAdapter threadAdapter;

    private FileDao mFileDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        mFileDao = new FileDaoImpl(this);

        fileList = new ArrayList<>();
        fileList.add(new FileInfo(1, "慕课网", "http://www.imooc.com/mobile/mukewang.apk", 0, 0, false));
        fileList.add(new FileInfo(2, "12306", "http://gdown.baidu.com/data/wisegame/2defe926519feba7/tielu12306_52.apk", 0, 0, false));
        fileList.add(new FileInfo(3, "追书神器", "http://dx2.9ht.com/ls/9ht.com.zhuishushenqi.zip", 0, 0, false));

        for (FileInfo fileInfo : fileList) {
            FileInfo fileInfo1 = mFileDao.selectFileByFileId(fileInfo.getId());
            if (fileInfo1 != null) {

                fileInfo.setIsFinished(fileInfo1.isFinished());
                fileInfo.setFinished(fileInfo1.getFinished());
            }
        }

        init();

    }

    private void init() {

        threadListView = (ListView) findViewById(R.id.threadListView);
        clearBtn = (Button) findViewById(R.id.clearBtn);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("确定删除所有下载文件？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //停止所有下载
                                Intent intent = new Intent(context, DownloadService.class);
                                intent.setAction(DownloadService.ACTION_STOP_ALL);
                                startService(intent);

                                //删除本地已下载文件及本地数据库下载任务信息
                                for (FileInfo fileInfo : fileList) {
                                    new ThreadDaoImpl(context).deleteThread(fileInfo.getUrl());//删除数据库下载任务信息
                                    new FileDaoImpl(context).deleteFileByFileId(fileInfo.getId());//删除数据库文件信息

                                    File file = new File(DownloadService.DOWNLOAD_PATH,
                                            fileInfo.getUrl().substring(fileInfo.getUrl().lastIndexOf("/") + 1));
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }

                                //重设文件列表
                                if (threadAdapter != null) {
                                    threadAdapter.resetFileList();
                                }

                            }
                        })
                        .setNegativeButton("取消", null).show();


            }
        });

        threadAdapter = new ThreadAdapter(context, fileList);

        threadListView.setAdapter(threadAdapter);

        //广播过滤器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.DOWNLOAD_COMPLETED);
        //注册广播
        mBroadcast = new MyBroadcast();
        registerReceiver(mBroadcast, filter);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

//            downloadBtn.performClick();//模拟点击
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //退出APP，停止所有下载任务，并更新本地下载信息
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.ACTION_QUIT);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcast);
    }

    //创建一个广播
    class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("file");

            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {//更新下载进度条

                //更新下载列表
                threadAdapter.updateProgress(fileInfo.getId(), fileInfo.getFinished());

            } else if (DownloadService.DOWNLOAD_COMPLETED.equals(intent.getAction())) {//下载完成

                //下载完成，改变按钮状态
                threadAdapter.setCompleted(fileInfo);

                Toast.makeText(context, fileInfo.getFileName() + "下载完成", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
