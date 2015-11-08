package com.lee.downloaddemo.db;

import com.lee.downloaddemo.entity.ThreadInfo;

import java.util.List;

/**
 * 数据库访问接口
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public interface ThreadDao {

    /**
     * 插入下载任务
     *
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除下载任务
     *
     * @param url
     */
    public void deleteThread(String url);

    /**
     * 更新下载进度
     *
     * @param id
     * @param url
     * @param finished
     */
    public void updateProgress(int id, String url, int finished);


    /**
     * 获取指定URL的所有下载任务
     *
     * @param url
     * @return
     */
    public List<ThreadInfo> queryThread(String url);

    /**
     * 获取所有下载任务
     *
     * @return
     */
    public List<ThreadInfo> queryAllThread();

    /**
     * 下载任务是否存在
     *
     * @param id
     * @param url
     */
    public boolean isExists(int id, String url);
}
