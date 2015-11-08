package com.lee.downloaddemo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lee.downloaddemo.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据操作接口实现
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public class ThreadDaoImpl implements ThreadDao {

    private DBHelper dbHelper;

    public ThreadDaoImpl(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    //加锁，同步任务，每次只能有一个对象插入数据到数据库
    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?, ?, ?, ?, ?)", new Object[]{threadInfo.getId(), threadInfo.getUrl(),
                threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?", new String[]{url});
        db.close();
    }

    @Override
    public synchronized void updateProgress(int id, String url, int finished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished=? where thread_id = ? and url = ?", new Object[]{
                finished, id, url
        });
        db.close();
    }

    @Override
    public ArrayList<ThreadInfo> queryThread(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});

        ArrayList<ThreadInfo> threadInfoList = new ArrayList<>();

        while (cursor.moveToNext()) {

            ThreadInfo threadInfo = new ThreadInfo(
                    cursor.getInt(cursor.getColumnIndex("thread_id")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    cursor.getInt(cursor.getColumnIndex("start")),
                    cursor.getInt(cursor.getColumnIndex("end")),
                    cursor.getInt(cursor.getColumnIndex("finished"))
            );

            threadInfoList.add(threadInfo);
        }

        return threadInfoList;
    }

    @Override
    public List<ThreadInfo> queryAllThread() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info", null);

        ArrayList<ThreadInfo> threadInfoList = new ArrayList<>();

        while (cursor.moveToNext()) {

            ThreadInfo threadInfo = new ThreadInfo(
                    cursor.getInt(cursor.getColumnIndex("thread_id")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    cursor.getInt(cursor.getColumnIndex("start")),
                    cursor.getInt(cursor.getColumnIndex("end")),
                    cursor.getInt(cursor.getColumnIndex("finished"))
            );

            threadInfoList.add(threadInfo);
        }

        return threadInfoList;
    }

    @Override
    public boolean isExists(int id, String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where thread_id = ? and url = ?", new String[]{id + "", url});

        if (cursor.moveToNext())
            return true;

        return false;

    }
}
