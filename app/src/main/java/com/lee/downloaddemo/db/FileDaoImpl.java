package com.lee.downloaddemo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lee.downloaddemo.entity.FileInfo;

/**
 * 文件数据访问接口实现类
 * <p/>
 * Created by Administrator on 2015/11/8.
 */
public class FileDaoImpl implements FileDao {

    private DBHelper dbHelper;

    public FileDaoImpl(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertFile(FileInfo fileInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into file_info(file_id, file_name, url, total_length, finished, is_finished)" +
                " values(?, ?, ?, ?, ?, ?)", new Object[]{fileInfo.getId(), fileInfo.getFileName(), fileInfo.getUrl(),
                fileInfo.getFinished(), String.valueOf(fileInfo.isFinished())});
    }

    @Override
    public synchronized void updateFile(FileInfo fileInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update file_info set finished=?, is_finished=? where file_id=?", new Object[]{fileInfo.getFinished(),
                String.valueOf(fileInfo.isFinished()), fileInfo.getId()});
    }

    @Override
    public FileInfo selectFileByFileId(int fileId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from file_info where file_id = ?", new String[]{fileId + ""});

        if (cursor.moveToNext()) {

            FileInfo fileInfo = new FileInfo(
                    cursor.getInt(cursor.getColumnIndex("file_id")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    cursor.getInt(cursor.getColumnIndex("total_length")),
                    cursor.getInt(cursor.getColumnIndex("finished")),
                    Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("is_finished")))
            );

            return fileInfo;
        }

        return null;
    }
}
