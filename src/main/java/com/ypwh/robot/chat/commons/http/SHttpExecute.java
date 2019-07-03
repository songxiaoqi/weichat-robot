package com.ypwh.robot.chat.commons.http;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * http执行器
 */
public interface SHttpExecute {
    /**
     * 设置连接超时时间
     */
    void setConnectTimeout(int timeout);

    /**
     * 获取连接超时时间
     */
    int getConnectTimeout();

    /**
     * 设置读取超时时间
     */
    void setReadTimeout(int timeout);

    /**
     * 获取读取超时时间
     */
    int getReadTimeout();

    /**
     * 添加cookie
     */
    void addCookie(URI uri, HttpCookie cookie);

    /**
     * 获取cookie
     */
    List<HttpCookie> getCookies(URI uri);

    /**
     * 获取所有cookie
     */
    List<HttpCookie> getCookies();

    /**
     * 删除cookie信息
     */
    void rmvCookies(URI uri, HttpCookie cookie);

    /**
     * 删除所有cookie
     */
    void rmvCookies();

    /**
     * 设置请求拦截器
     */
    void setInterceptors(SInterceptor... interceptors);

    /**
     * 获取请求拦截器
     */
    SInterceptor[] getInterceptors();

    /**
     * 执行http请求
     */
    SResponse execute(SRequest request) throws Exception;

}
