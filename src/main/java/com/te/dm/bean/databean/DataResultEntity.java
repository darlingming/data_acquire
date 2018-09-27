package com.te.dm.bean.databean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * @author DM
 */

public class DataResultEntity {

    @JSONField(ordinal = 0)
    private String serialNumber;

    @JSONField(ordinal = 1)
    private String code;

    @JSONField(ordinal = 2)
    private String message;

    @JSONField(ordinal = 3, format = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    @JSONField(ordinal = 4)
    private DataEntity data;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public DataEntity getData() {
        return data;
    }

    public void setData(DataEntity data) {
        this.data = data;
    }
}
