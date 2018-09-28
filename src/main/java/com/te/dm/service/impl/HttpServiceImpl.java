package com.te.dm.service.impl;

import com.alibaba.fastjson.JSON;
import com.te.dm.bean.Service;
import com.te.dm.bean.databean.DataResultEntity;
import com.te.dm.common.http.HttpManager;
import com.te.dm.common.http.HttpTask;
import com.te.dm.service.HttpService;
import com.te.dm.utils.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author DM
 */
public class HttpServiceImpl implements HttpService {
    private static final Logger logger = LogManager.getLogger(HttpServiceImpl.class);


    /**
     * 线程数据调用初始化
     *
     * @param service
     * @param count
     * @param bw
     * @throws IOException
     */
    private void initThreadData(final Service service, final int count, final BufferedWriter bw) throws IOException {
        int minNum = 0;
        int maxNum = count;
        int v = maxNum / 5000;//默认线程控制为5000条
        int threadCount = v > service.getThreadNum() ? service.getThreadNum() : v == 0 ? 1 : v;

        ExecutorService executor = null;
        CountDownLatch cdl;

        try {
            int lots = maxNum / threadCount;
            threadCount += (maxNum - lots * threadCount > 0) ? 1 : 0;
            executor = Executors.newFixedThreadPool(threadCount);
            cdl = new CountDownLatch(threadCount);
            final Map<Integer, Integer> failMap = new HashMap<Integer, Integer>() {
                {
                    put(-1, 0);
                }
            };
            logger.info("ThreadCount>" + threadCount);
            for (int i = 0; i < threadCount; i++) {
                maxNum = minNum + lots;
                maxNum = maxNum > count ? count : maxNum;
                HttpTask myTask = new HttpTask(service, cdl, minNum + 1, maxNum, failMap, bw);
                executor.execute(myTask);
                minNum = maxNum;
            }
            cdl.await();
        } catch (Exception e) {
            logger.error("Exception:", e);
        } finally {
            executor.shutdown();
            bw.close();

        }
    }

    @Override
    public int execute(Service service) throws IOException {
        logger.info("start service ");

        HttpManager httpManager = HttpManager.getInstance();
        httpManager.setHttp(service.getHttp());

        String cuurDate = service.getHttp().getDataTime();

        if (null == cuurDate || cuurDate.isEmpty()) {
            cuurDate = DateUtils.formatDateTime(new Date(), DateUtils.parsePatterns[6]);
        }
        String key = cuurDate + service.getHttp().getProvice() + service.getHttp().getDataType();
        String queryCountUrl = service.getHttp().getBaseUrl() + "?action=" + service.getHttp().getActionCount() + "&user_token=" + service.getHttp().getToken() + "&key=" + key + "&serialNumber=" + System.currentTimeMillis();
        String valueCountJson = httpManager.httpGet(queryCountUrl);
        logger.info("获取值>" + valueCountJson);

        if (null != valueCountJson) {
            DataResultEntity result = JSON.parseObject(valueCountJson, DataResultEntity.class);
            if ("20000".equals(result.getCode())) {
                String value = result.getData().getValue();
                logger.info("value>" + value);

                if (null != value && !"null".equals(value)) {
                    int count = Integer.valueOf(value);
                    String filePath = service.getDownLoadFilePath() + File.separator + cuurDate;
                    StringBuffer sb = new StringBuffer();
                    sb.append(cuurDate);
                    if(null != service.getHttp().getProvice() && !service.getHttp().getProvice().isEmpty()){
                        sb.append("_" + service.getHttp().getProvice());
                    }
                    if(null != service.getHttp().getDataType() && !service.getHttp().getDataType().isEmpty()){
                        sb.append("_" + service.getHttp().getDataType());
                    }
                    sb.append("_" + System.currentTimeMillis() + ".DAT");
                    String fileName = sb.toString();
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + File.separator + fileName, true), "utf8"), 1024);
                    this.initThreadData(service, count, bw);
                } else {
                    logger.warn("获取value值为空>" + valueCountJson);
                }
            } else {
                logger.warn("获取值无数据>" + valueCountJson);
            }

        } else {
            logger.warn("获valueCountJson取值为空>" + valueCountJson);
        }


        return 0;
    }

    public static void main(String[] args) {
        logger.info(File.pathSeparator);
    }
}


