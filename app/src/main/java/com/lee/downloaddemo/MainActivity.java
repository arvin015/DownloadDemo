package com.lee.downloaddemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        fileList = new ArrayList<>();
        fileList.add(new FileInfo(1, "http://www.imooc.com/mobile/mukewang.apk", 0, 0, false));
        fileList.add(new FileInfo(2, "http://gdown.baidu.com/data/wisegame/2defe926519feba7/tielu12306_52.apk", 0, 0, false));
        fileList.add(new FileInfo(3, "http://dx2.9ht.com/ls/9ht.com.zhuishushenqi.zip", 0, 0, false));

        init();

    }

    private void init() {

        threadListView = (ListView) findViewById(R.id.threadListView);
        clearBtn = (Button) findViewById(R.id.clearBtn);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FileInfo fileInfo : fileList) {
                    new ThreadDaoImpl(context).deleteThread(fileInfo.getUrl());

                    File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
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
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcast);
    }

    //创建一个广播
    class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int fileId = intent.getIntExtra("fileId", 0);

            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {//更新下载进度条

                int finished = intent.getIntExtra("finished", 0);

                //更新下载列表
                threadAdapter.updateProgress(fileId, finished);

            } else if (DownloadService.DOWNLOAD_COMPLETED.equals(intent.getAction())) {//下载完成

                String fileName = intent.getStringExtra("fileName");

                //下载完成，改变按钮状态
                threadAdapter.setCompleted(fileId);

                Toast.makeText(context, fileName + "下载完成", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
