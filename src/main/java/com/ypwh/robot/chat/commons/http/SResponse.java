package com.ypwh.robot.chat.commons.http;

import java.io.File;
import java.io.InputStream;

/**
 * 响应接口
 */
public interface SResponse {

    /**
     * 获取输入流
     */
    InputStream stream();

    /**
     * 获取字符串结果(默认编码)
     */
    String string();

    /**
     * 获取字符串结果
     *
     * @param charset 字符串编码
     */
    String string(String charset);

    /**
     * 获取文件结果
     *
     * @param path 文件路径
     */
    File file(String path);
}
