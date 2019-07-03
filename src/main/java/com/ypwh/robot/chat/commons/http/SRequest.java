package com.ypwh.robot.chat.commons.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.ypwh.robot.chat.commons.http.impl.MultipartContent;
import com.ypwh.robot.chat.commons.http.impl.Part;
import com.ypwh.robot.chat.commons.http.impl.UrlEncodeContent;

/**
 * 请求接口
 */
public interface SRequest {

    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "text/xml";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    /**
     * 设置请求方法
     */
    void setMethod(String method);

    /**
     * 获取请求方法
     */
    String getMethod();

    /**
     * 设置请求url
     */
    void setUrl(String url);

    /**
     * 获取请求url
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


    /**
     * 拼接get方式的请求参数
     */
    default String kvJoin(List<KeyValue> keyValues, String charset) throws UnsupportedEncodingException {
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
    default SRequest query(String key, Object value) {
        return query(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求地址参数，可选择对于同名的请求地址参数的处理方式
     * @param append true：清除已经存在的同名的请求地址参数，false：追加同名的请求地址参数
     */
    default SRequest query(String key, Object value, boolean append) {
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
    default SRequest header(String key, String value) {
        return header(key, value, false);
    }

    /**
     * 添加HTTP请求头，可选择对于同名的请求头的处理方式
     */
    default SRequest header(String key, String value, boolean append) {
        setHeader(key, value, append);
        return this;
    }

    /**
     * 添加请求体参数，允许同名的请求体参数。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     */
    default SRequest content(String key, Object value) {
        return content(key, value, false);
    }

    /**
     * 添加HTTP请求体参数
     * 如果有文件参数，则使用multipart请求，否则使用urlencoded请求
     */
    default SRequest content(String key, Object value, boolean append) {
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
    default SRequest content(Content content) {
        setContent(content);
        return this;
    }


}
