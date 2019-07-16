package com.ypwh.http;

import java.io.File;
import java.io.InputStream;

/**
 * 相应接口
 */
public interface Response extends AutoCloseable{

    /**
     * 获取输入流
     *
     * @return 获取输入流
     */
    InputStream stream();

    /**
     * 获取字符串结果(默认编码)
     *
     * @return 字符串结果
     */
    String string();

    /**
     * 获取字符串结果
     *
     * @param charset 字符串编码
     * @return 字符串结果
     */
    String string(String charset);

    /**
     * 获取文件结果
     *
     * @param path 文件路径
     * @return 文件结果
     */
    File file(String path);
}
