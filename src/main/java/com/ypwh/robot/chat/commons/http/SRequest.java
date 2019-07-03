package com.ypwh.robot.chat.commons.http;

import java.util.List;
/**
 * 请求接口
 */
public interface SRequest {

    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "text/xml";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    /**
     * 设置请求方法
     */
    void setMethod(String method);

    /**
     * 获取请求方法
     */
    String getMethod();

    /**
     * 设置请求url
     */
    void setUrl(String url);

    /**
     * 获取请求url
     */
    String getUrl();

    /**
     * 设置请求头部
     *
     * @param key    请求头键
     * @param value  请求头值
     * @param append 重复请求头是否追加
     */
    void setHeader(String key, String value, boolean append);

    /**
     * 获取请求头部
     */
    List<KeyValue> getHeaders();

    /**
     * 设置请求体
     *
     * @param content 请求体
     */
    void setContent(Content content);

    /**
     * 获取请求体
     *
     * @return 请求体信息
     */
    Content getContent();

}
