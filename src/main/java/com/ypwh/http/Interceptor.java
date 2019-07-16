package com.ypwh.http;

/**
 * 自定义请求拦截器
 */
public interface Interceptor {


    /**
     * 拦截请求
     * @param executor 执行器
     * @param request 请求
     * @return Response 响应
     * @throws Exception 异常
     */
    Response intercept(HttpExecutor executor,Request request) throws Exception;
}
