package com.te.dm.bean;

public class Ftp {
    /**
     * 服务器地址
     */
    private String hostName;
    /**
     * 服务器端口
     */
    private int port;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;
    /**
     * FTP根目录.
     */
    private String remote_path;

    /**
     * FTP当前目录.
     */
    private String currentPath = "";

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemote_path() {
        return remote_path;
    }

    public void setRemote_path(String remote_path) {
        this.remote_path = remote_path;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }
}
