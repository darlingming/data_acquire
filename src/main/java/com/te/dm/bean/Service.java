package com.te.dm.bean;

public class Service {
    private String name;
    private String serviceClass;
    private String downLoadFilePath;
    private int threadNum;
    //!--失败重试次数-->
    private int failedRetryTime;
    //!--失败次数累积退出-->
    private int failedExitTimes;
    //!--失败重试睡眠时间-->
    private int retrySleepTime;
    //token

    private Http http;

    private Shell shell;

    public String getName() {
        return name;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public String getDownLoadFilePath() {
        return downLoadFilePath;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public int getFailedRetryTime() {
        return failedRetryTime;
    }

    public int getFailedExitTimes() {
        return failedExitTimes;
    }

    public int getRetrySleepTime() {
        return retrySleepTime;
    }

    public Http getHttp() {
        return http;
    }

    public Shell getShell() {
        return shell;
    }
}
