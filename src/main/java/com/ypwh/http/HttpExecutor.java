package com.ypwh.http;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import com.ypwh.http.impl.DefaultResponse;

/**
 * http执行器接口
 */
public interface HttpExecutor {

    /**
     * 执行请求返回响应
     * @param request 请求
     * @return Response 响应
     * @throws Exception 发生异常
     */
    DefaultResponse execute(Request request)throws Exception;

    /**
     * 设置请求拦截器
     * @param interceptor 请求拦截器
     */
    void setInterceptors(Interceptor... interceptor);

    /**
     * 获取所有设置的请求拦截器
     * @return Interceptor[] 拦截器数组
     */
    Interceptor[] getInterceptors();

    /**
     * 设置连接超时时间
     * @param timeout 单位：ms
     */
    void setConnectTimeout(int timeout);

    /**
     * 设置读取响应的超时时间
     * @param timeout 单位：ms
     */
    void setReadTimeout(int timeout);

    /**
     * 获取连接超时时间
     * @return timeout 超时时间
     */
    int getConnectTimeout();

    /**
     * 获取响应超时时间
     * @return timeout 超时时间
     */
    int getReadTimeout();

    /**
     * 添加cookie
     * @param uri uri
     * @param cookie cookie
     */
    void addCookie(URI uri, HttpCookie cookie);

    /**
     * 获取指定的cookies
     * @param uri uri
     * @return List<HttpCookie>
     */
    List<HttpCookie> getCookies(URI uri);

    /**
     * 获取所有的cookie
     * @return List<HttpCookie>
     */
    List<HttpCookie> getCookies();

    /**
     * 移除cookie
     * @param uri uri
     * @param cookie cookie
     */
    void rmCookie(URI uri, HttpCookie cookie);

    /**
     * 移除所有的cookie
     */
    void rmAllCookies();
}
