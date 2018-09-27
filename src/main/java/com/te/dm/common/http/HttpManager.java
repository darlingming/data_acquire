package com.te.dm.common.http;


import com.te.dm.bean.Http;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author DM
 */
public class HttpManager {
    private static final Logger logger = LogManager.getLogger(HttpManager.class);

    private static HttpManager hcm = null;

    public org.apache.http.client.CookieStore cookieStore = new BasicCookieStore();
    private PoolingHttpClientConnectionManager connMgr = null;
    private HttpRequestRetryHandler myRetryHandler = null;
    // 配置超时时间
    private RequestConfig requestConfig = null;
    private Http http;

    /**
     * 获取实例
     *
     * @return
     */
    public static HttpManager getInstance() {
        if (null == hcm) {
            synchronized (logger) {
                hcm = new HttpManager();
            }
        }
        return hcm;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    /**
     * 自定义连接存活策略
     *
     * @param response
     * @param context
     * @return
     */
    private ConnectionKeepAliveStrategy myStrategy = null;
    private CloseableHttpClient closeableHttpClient = null;

    private synchronized CloseableHttpClient getHttpCilent() {

        if (null == closeableHttpClient) {
            synchronized (logger) {
                closeableHttpClient = HttpClients.custom() //aa
                        //.setDefaultCookieStore(cookieStore) //bb
                        .setKeepAliveStrategy(myStrategy) //cc
                        .setConnectionManager(connMgr) //cdd
                        .setRetryHandler(myRetryHandler) //fff
                        .build();
            }
        }


        return closeableHttpClient;
    }

    /**
     * 初始化构造函数
     */
    private HttpManager() {

        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();

        connMgr = new PoolingHttpClientConnectionManager(registry) {
            {
                if (null != http) {
                    setMaxTotal(http.getMaxTotal());
                    setDefaultMaxPerRoute(http.getMaxPerRoute());
                } else {
                    setMaxTotal(500);
                    setDefaultMaxPerRoute(2001);
                }


            }
        };

        myRetryHandler = (IOException exception, int executionCount, HttpContext context) -> {
            if (executionCount >= 5) {// 如果超过最大重试次数，那么就不要继续了
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            //if (exception instanceof UnknownHostException) {// 目标服务器不可达
            //    return false;
            //}
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝

                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            if (!(HttpClientContext.adapt(context).getRequest() instanceof HttpEntityEnclosingRequest)) { // 如果请求是幂等的，就再次尝试
                return true;
            }

            return false;

        };

        myStrategy = (HttpResponse response, HttpContext context) -> {
            // Honor 'keep-alive' header
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        logger.warn("CONN_KEEP_ALIVE value=" + value);
                        return Long.parseLong(value) * 1000;
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            if ("domain".equalsIgnoreCase(target.getHostName())) {
                // Keep alive for 5 seconds only
                return 5 * 1000;
            } else {
                // otherwise keep alive for 30 seconds
                return 30 * 1000;
            }
        };
        if (null != http) {
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(http.getConnectTimeout())    // 请求超时时间
                    .setConnectionRequestTimeout(http.getConnectionRequestTimeout()) // 连接超时时间
                    .setSocketTimeout(http.getSocketTimeout())   // 等待数据超时时间
                    .setRedirectsEnabled(true) // 默认允许自动重定向
                    .build();
        } else {
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)    // 请求超时时间
                    .setConnectionRequestTimeout(10 * 1000) // 连接超时时间
                    .setSocketTimeout(10000)   // 等待数据超时时间
                    .setRedirectsEnabled(true) // 默认允许自动重定向
                    .build();
        }


    }


    /**
     * @param url
     * @param list
     * @return
     */
    public String httpPost(String url, List<BasicNameValuePair> list) throws IOException {

        HttpPost httpPost = new HttpPost(url);
        httpPost.releaseConnection();
        // 设置超时时间
        httpPost.setConfig(requestConfig);

        String strResult = "";
        int statusCode = 404;
        try {

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
            // 设置post求情参数
            httpPost.setEntity(entity);
            CloseableHttpResponse httpResponse = getHttpCilent().execute(httpPost);

            if (httpResponse != null) {
                statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    strResult = EntityUtils.toString(httpResponse.getEntity());
                    //logger.info("post/" + statusCode + ":" + strResult);
                    return strResult;
                } else {
                    strResult = "Error  Response: " + httpResponse.getStatusLine().toString();
                    logger.info("post/" + statusCode + ":" + strResult);
                    strResult = null;
                }

                httpResponse.close();
            } else {

            }


        } finally {
            httpPost.releaseConnection();
        }

        return strResult;
    }

    /**
     * @param url
     * @return
     */
    public String httpGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        String srtResult = null;
        int statusCode = 404;
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = getHttpCilent().execute(httpGet);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                srtResult = EntityUtils.toString(httpResponse.getEntity());// 获得返回的结果
                //logger.info("get/ " + statusCode + ":" + srtResult);
                return srtResult;
            } else {
                //srtResult = EntityUtils.toString(httpResponse.getEntity());// 获得返回的结果
                //logger.info("get/ " + statusCode + ":" + srtResult);
                return null;
            }
        } finally {
            httpGet.releaseConnection();
        }
    }


    public void setCookieStore(List<BasicClientCookie> cookielist) {
        for (BasicClientCookie cookie : cookielist) {
            cookieStore.addCookie(cookie);
        }
    }

    public void createCookie(List<BasicClientCookie> cookielist) {
        for (BasicClientCookie cookie : cookielist) {
            cookieStore.addCookie(cookie);
        }
    }

    public void close() throws IOException {
        this.connMgr.closeExpiredConnections(); // 关闭失效的连接
        // 可选的, 关闭30秒内不活动的连接
        this.connMgr.closeIdleConnections(50, TimeUnit.SECONDS);
    }


    private void setPostParams(HttpPost httpost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void config(HttpRequestBase httpRequestBase) {
        // 设置Header等
        // httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        // httpRequestBase
        // .setHeader("Accept",
        // "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpRequestBase.setHeader("Accept-Language",
        // "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        // httpRequestBase.setHeader("Accept-Charset",
        // "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");


        httpRequestBase.setConfig(requestConfig);
    }
}
