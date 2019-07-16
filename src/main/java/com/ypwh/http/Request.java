package com.ypwh.http;

import java.util.List;

/**
 * 请求接口
 */
public interface Request {

    /**
     * 设置请求方法
     *
     * @param method 请求方法
     */
    void setMethod(String method);

    /**
     * 获取请求方法
     *
     * @return 请求方法
     */
    String getMethod();

    /**
     * 设置请求url
     *
     * @param url 请求url
     */
    void setUrl(String url);

    /**
     * 获取请求url
     *
     * @return 请求url
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
     *
     * @return 请求头部
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
