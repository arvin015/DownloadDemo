package com.lee.downloaddemo.entity;

import java.io.Serializable;

/**
 * 文件信息实体类
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public class FileInfo implements Serializable {

    private int id;
    private String fileName;
    private String url;
    private int totalLength;
    private int finished;
    private boolean isFinished;

    public FileInfo() {
    }

    public FileInfo(int id, String fileName, String url, int totalLength, int finished, boolean isFinished) {
        this.id = id;
        this.url = url;
        this.totalLength = totalLength;
        this.finished = finished;
        this.isFinished = isFinished;
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", url='" + url + '\'' +
                ", totalLength=" + totalLength +
                ", finished=" + finished +
                ", isFinished=" + isFinished +
                '}';
    }
}
