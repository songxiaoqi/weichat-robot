package com.ypwh.robot.chat.commons.http.impl;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import com.ypwh.robot.chat.commons.http.Content;
import com.ypwh.robot.chat.commons.http.KeyValue;
import com.ypwh.robot.chat.commons.http.SRequest;
import com.ypwh.robot.chat.commons.utils.STools;

import static java.nio.charset.Charset.defaultCharset;


/**
 * 默认的http请求实现类
 */
public final class DefaultRequest implements SRequest {
    /**
     * 请求编码方式
     */
    private String charset;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 请求地址
     */
    private String requestUrl;
    /**
     * 请求地址参数
     */
    private List<KeyValue> requestQueries;
    /**
     * 请求头
     */
    private List<KeyValue> requestHeaders;
    /**
     * 请求体
     */
    private Content requestContent;


    private DefaultRequest(String method, String url) {
        defaultCharset();
        setMethod(method);
        setUrl(url);
    }

    /**
     * 新建一个GET请求
     */
    public static SRequest GET(String url) {
        return new DefaultRequest(METHOD_GET, url);
    }

    /**
     * 新建一个POST请求
     */
    public static SRequest POST(String url) {
        return new DefaultRequest(METHOD_POST, url);
    }

    /**
     * 新建一个PUT请求
     */
    public static SRequest PUT(String url) {
        return new DefaultRequest(METHOD_PUT, url);
    }

    /**
     * 新建一个DELETE请求
     */
    public static SRequest DELETE(String url) {
        return new DefaultRequest(METHOD_DELETE, url);
    }



    @Override
    public void setMethod(String method) {
        if (method.equals(METHOD_GET) || method.equals(METHOD_POST) || method.equals(METHOD_DELETE) || method.equals(METHOD_PUT)) {
            this.requestMethod = method;
        }
    }

    /**
     * 获得HTTP请求的请求方法
     */
    @Override
    public String getMethod() {
        return this.requestMethod;
    }

    @Override
    public void setUrl(String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("XRequest仅支持HTTP协议和HTTPS协议");
        } else if (url.indexOf('?') >= 0) {
            this.requestUrl = url.substring(0, url.indexOf('?'));
            try {
                for (String keyValue : url.substring(url.indexOf('?') + 1).split("&")) {
                    int eqIndex = keyValue.indexOf('=');
                    if (eqIndex < 0) {
                        throw new IllegalArgumentException("请求的url格式有误");
                    } else {
                        query(URLDecoder.decode(keyValue.substring(0, eqIndex), charset), URLDecoder.decode(keyValue.substring(eqIndex + 1), charset));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(String.format("不支持编码方式：%s", charset));
            }
        } else {
            this.requestUrl = url;
        }
    }

    /**
     * 获得HTTP请求的请求url，如果有请求地址参数则自动拼接成带参数的url
     */
    @Override
    public String getUrl() {
        try {
            if (this.requestQueries != null) {
                return String.format("%s?%s", this.requestUrl, kvJoin(this.requestQueries, charset));
            } else {
                return this.requestUrl;
            }
        } catch (Exception e) {
            throw new IllegalStateException("生成请求url时出错");
        }
    }

    @Override
    public void setHeader(String key, String value, boolean append) {
        Objects.requireNonNull(key);
        if (this.requestHeaders == null) {
            this.requestHeaders = new LinkedList<>();
        }
        if (append) {
            Iterator<KeyValue> iterator = this.requestHeaders.iterator();
            while (iterator.hasNext()) {
                KeyValue keyValue = iterator.next();
                if (keyValue.key.equals(key)) {
                    iterator.remove();
                }
            }
        }
        if (value != null) {
            requestHeaders.add(new KeyValue(key, value));
        }
    }

    /**
     * 获得HTTP请求的请求头列表
     */
    @Override
    public List<KeyValue> getHeaders() {
        if ((this.requestMethod.equals(METHOD_POST) || this.requestMethod.equals(METHOD_PUT)) && this.requestContent != null) {
            try {
                header("Content-Type", this.requestContent.contentType(), true);
                long contentLength = requestContent.contentLength();
                if (contentLength > 0) {
                    header("Content-Length", String.valueOf(contentLength), true);
                } else {
                    header("Transfer-Encoding", "chunked", true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.requestHeaders;
    }

    @Override
    public void setContent(Content content) {
        Objects.requireNonNull(content);
        this.requestContent = content;
    }

    /**
     * 获得HTTP请求的请求体
     */
    @Override
    public Content getContent() {
        return this.requestContent;
    }

}
