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

    public static DefaultRequest GET(String url) {
        return new DefaultRequest(METHOD_GET, url);
    }

    public static DefaultRequest POST(String url) {
        return new DefaultRequest(METHOD_POST, url);
    }

    public static DefaultRequest PUT(String url) {
        return new DefaultRequest(METHOD_PUT, url);
    }

    public static DefaultRequest DELETE(String url) {
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


    /**
     * 拼接get方式的请求参数
     */
    public String kvJoin(List<KeyValue> keyValues, String charset) throws UnsupportedEncodingException {
        StringBuilder sbStr = new StringBuilder();
        for (KeyValue keyValue : keyValues) {
            if (sbStr.length() > 0) {
                sbStr.append('&');
            }
            sbStr.append(URLEncoder.encode(keyValue.key, charset)).append('=').append(URLEncoder.encode(String.valueOf(keyValue.value), charset));
        }
        return sbStr.toString();
    }


    /**
     * 添加请求参数，键重复则替换值
     */
    public SRequest query(String key, Object value) {
        return query(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求地址参数，可选择对于同名的请求地址参数的处理方式
     * @param append true：清除已经存在的同名的请求地址参数，false：追加同名的请求地址参数
     */
    public SRequest query(String key, Object value, boolean append) {
        Objects.requireNonNull(key);
        if (this.requestQueries == null) {
            this.requestQueries = new LinkedList<>();
        }
        if (append) {
            Iterator<KeyValue> iterator = this.requestQueries.iterator();
            while (iterator.hasNext()) {
                KeyValue keyValue = iterator.next();
                if (keyValue.key.equals(key)) {
                    iterator.remove();
                }
            }
        }
        if (value != null) {
            this.requestQueries.add(new KeyValue(key, value));
        }
        return this;
    }

    /**
     * 添加请求头，键重复则替换值
     */
    public SRequest header(String key, String value) {
        return header(key, value, false);
    }

    /**
     * 添加HTTP请求头，可选择对于同名的请求头的处理方式
     */
    public SRequest header(String key, String value, boolean append) {
        setHeader(key, value, append);
        return this;
    }

    /**
     * 添加请求体参数，允许同名的请求体参数。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     */
    public SRequest content(String key, Object value) {
        return content(key, value, false);
    }

    /**
     * 添加HTTP请求体参数
     * 如果有文件参数，则使用multipart请求，否则使用urlencoded请求
     */
    public SRequest content(String key, Object value, boolean append) {
        Objects.requireNonNull(key);
        if (METHOD_GET.equals(requestMethod) || METHOD_DELETE.equals(requestMethod)) {
            throw new IllegalArgumentException(String.format("%s方法不能添加请求体", requestMethod));
        }
        if (this.requestContent == null) {
            this.requestContent = new UrlEncodeContent();
        }
        if (this.requestContent instanceof UrlEncodeContent) {
            UrlEncodeContent urlencodedContent = ((UrlEncodeContent) this.requestContent);
            if (value instanceof File || value instanceof Part) {
                //如果请求体一开始是urlencoded类型的，现在来了一个文件，则自动转换成multipart类型的，然后交给multipart类型的处理逻辑处理
                MultipartContent multipartContent = new MultipartContent();
                for (KeyValue keyValue : urlencodedContent.getParams()) {
                    multipartContent.part(keyValue.key, keyValue.value);
                }
                this.requestContent = multipartContent;
            } else {
                urlencodedContent.param(key, value, append);
                return this;
            }
        }
        if (this.requestContent instanceof MultipartContent) {
            MultipartContent multipartContent = (MultipartContent) this.requestContent;
            if (value instanceof Part) {
                Part part = (Part) value;
                if (key.equals(part.name)) {
                    multipartContent.part((Part) value, append);
                } else {
                    throw new IllegalArgumentException(String.format("参数的key：%s与表单的名称：%s不相等", key, part.name));
                }
            } else {
                multipartContent.part(key, value, append);
            }
            return this;
        }
        throw new IllegalStateException(String.format("%s不能接受键值对请求体", this.requestContent.getClass().getName()));
    }

    /**
     * 添加自定义的HTTP请求体
     */
    public SRequest content(Content content) {
        setContent(content);
        return this;
    }

}
