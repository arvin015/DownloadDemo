package com.lee.downloaddemo.db;

import com.lee.downloaddemo.entity.FileInfo;

/**
 * 文件数据访问接口
 * <p/>
 * Created by Administrator on 2015/11/8.
 */
public interface FileDao {

    /**
     * 插入文件信息
     *
     * @param fileInfo
     */
    public void insertFile(FileInfo fileInfo);

    /**
     * 更新下载完成进度，及是否完成状态
     *
     * @param fileInfo
     */
    public void updateFile(FileInfo fileInfo);

    /**
     * 查询执行文件ID的文件信息
     *
     * @param fileId
     * @return
     */
    public FileInfo selectFileByFileId(int fileId);

    /**
     * 删除文件下载记录
     *
     * @param fileId
     */
    public void deleteFileByFileId(int fileId);
}
