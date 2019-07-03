package com.ypwh.robot.chat.commons.http;

/**
 * 拦截器
 */
public interface SInterceptor {

    /**
     * @param sHttpExecute http执行器
     * @param request  请求参数
     * @return 请求结果
     * @throws Exception 拦截过程中可能会发生异常
     */
    SResponse intercept(SHttpExecute sHttpExecute, SRequest request) throws Exception;
}
