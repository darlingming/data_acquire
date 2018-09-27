package com.te.dm.common.http;

import com.alibaba.fastjson.JSON;
import com.te.dm.bean.Service;
import com.te.dm.bean.databean.DataResultEntity;
import com.te.dm.utils.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class HttpTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpTask.class);
    private CountDownLatch cdl;
    private int minNum;
    private int startNum;
    private int maxNum;
    private Service service;
    private Map<Integer, Integer> failMap;
    private BufferedWriter bw;

    public HttpTask(final Service service, final CountDownLatch cdl, int minNum, int maxNum, final Map<Integer, Integer> failMap, final BufferedWriter bw) {
        //logger.info(minNum + "===" + maxNum);
        this.service = service;
        this.cdl = cdl;
        this.minNum = minNum;
        this.startNum = minNum;
        this.maxNum = maxNum;
        this.failMap = failMap;
        this.bw = bw;
    }

    /**
     * 校验失败次数
     */
    private void checkData() {
        Integer failValue = this.failMap.get(minNum);
        if (failValue != null) {
            if (failValue > service.getFailedRetryTime()) {
                this.failMap.put(-1, this.failMap.get(-1).intValue() + 1);
                minNum++;
            }
            this.failMap.put(minNum, failValue.intValue() + 1);
        } else {
            this.failMap.put(minNum, 1);
        }
    }

    @Override
    public void run() {
        try {

            while (minNum <= maxNum) {
                if (this.failMap.get(-1).intValue() > service.getFailedExitTimes()) {
                    logger.warn("Fail more");
                    break;
                }
                try {
                    HttpManager httpManager = HttpManager.getInstance();
                    httpManager.setHttp(service.getHttp());
                    String cuurDate = service.getHttp().getDataTime();

                    if (null == cuurDate || cuurDate.isEmpty()) {
                        cuurDate = DateUtils.formatDateTime(new Date(), DateUtils.parsePatterns[6]);
                    }
                    String key = cuurDate + service.getHttp().getProvice() + service.getHttp().getDataType() + minNum;
                    List<BasicNameValuePair> list = new ArrayList<>();

                    list.add(new BasicNameValuePair("action", service.getHttp().getActionInfo()));
                    list.add(new BasicNameValuePair("user_token", service.getHttp().getToken()));
                    list.add(new BasicNameValuePair("key", key));
                    list.add(new BasicNameValuePair("serialNumber", System.currentTimeMillis() + ""));


                    String valueJson = httpManager.httpPost(service.getHttp().getBaseUrl(), list);
                    if (null != valueJson) {
                        DataResultEntity result = JSON.parseObject(valueJson, DataResultEntity.class);
                        if ("20000".equals(result.getCode())) {
                            bw.write(result.getData().getValue() + "\r\n");
                            minNum++;
                        } else {
                            this.checkData();
                            logger.warn("Task WARN:" + key + "=" + valueJson);
                        }

                    } else {
                        this.checkData();
                    }
                } catch (Exception e) {
                    logger.error("Task:" + minNum, e);
                    this.checkData();
                    try {
                        Thread.currentThread().sleep(service.getRetrySleepTime());
                    } catch (InterruptedException e1) {
                        logger.error("Task Sleep:", e1);
                    }
                }
            }
        } finally {
            try {
                bw.flush();
            } catch (IOException e) {
                logger.error("flush>", e);
            }
            cdl.countDown();
        }

        logger.info("Task " + startNum + "-" + maxNum + "条:" + "执行完毕");
    }

    public static void main(String[] args) {
        Map<Integer, Integer> aa = new HashMap<Integer, Integer>();
        Integer a = aa.get(100);
        logger.info(a);
    }
}
