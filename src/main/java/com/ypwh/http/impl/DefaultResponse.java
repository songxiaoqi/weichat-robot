package com.ypwh.http.impl;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

import utils.StringUtils;

import com.ypwh.http.Response;

/**
 * 响应默认实现类
 */
public class DefaultResponse implements Response {

    private final HttpURLConnection connection;
    private final InputStream stream;

    public DefaultResponse(HttpURLConnection connection, InputStream inStream) {
        this.connection=connection;
        this.stream=inStream;
    }

    /**
     * 获取返回的输入流
     *
     * @return 连接的输入流，记得使用XResponse实例的close()方法关闭输入流和连接
     */
    @Override
    public InputStream stream() {
        return this.stream;
    }

    @Override
    public String string() {
        return string("utf-8");
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串
     *
     * @return 转化后的字符串
     */
    @Override
    public final String string(String charset) {
        try {
            return StringUtils.streamToStr(stream(), charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将连接返回的输入流中的数据转化成文件
     *
     * @param path 文件存储的路径
     * @return 转化后的文件
     */
    @Override
    public final File file(String path) {
        try {
            return StringUtils.streamToFile(stream(), path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (stream != null) {
            stream.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }



}
