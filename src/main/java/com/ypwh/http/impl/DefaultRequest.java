package com.ypwh.http.impl;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xuxiaoxiao.xtools.common.http.executor.impl.XRequest;
import utils.StringUtils;

import com.ypwh.http.Content;
import com.ypwh.http.KeyValue;
import com.ypwh.http.Request;

public class DefaultRequest implements Request {

    public static final String CHARSET_DEFAULT = "utf-8";

    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "text/xml";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

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


    public DefaultRequest(String method, String url) {
        this.charset=CHARSET_DEFAULT;
        setMethod(method);
        setUrl(url);
    }

    /**
     * 新建一个GET请求
     *
     * @param url 请求url
     * @return GET请求
     */
    public static DefaultRequest GET(String url) {
        return new DefaultRequest(GET, url);
    }

    /**
     * 新建一个POST请求
     *
     * @param url 请求url
     * @return POST请求
     */
    public static DefaultRequest POST(String url) {
        return new DefaultRequest(POST, url);
    }

    /**
     * 新建一个PUT请求
     *
     * @param url 请求url
     * @return PUT请求
     */
    public static DefaultRequest PUT(String url) {
        return new DefaultRequest(PUT, url);
    }

    /**
     * 新建一个DELETE请求
     *
     * @param url 请求url
     * @return DELETE请求
     */
    public static DefaultRequest DELETE(String url) {
        return new DefaultRequest(DELETE, url);
    }

    /**
     * 将键值对集合经过url编码后用固定的字符串连接起来
     *
     * @return 连接后的字符串
     * @throws UnsupportedEncodingException 给定的编码格式不支持时抛出异常
     */
    private static String kvJoin(List<KeyValue> keyValues, String charset) throws UnsupportedEncodingException {
        StringBuilder sbStr = new StringBuilder();
        for (KeyValue keyValue : keyValues) {
            if (sbStr.length() > 0) {
                sbStr.append('&');
            }
            sbStr.append(URLEncoder.encode(keyValue.key, charset)).append('=').append(URLEncoder.encode(String.valueOf(keyValue.value), charset));
        }
        return sbStr.toString();
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return this.charset;
    }

    /**
     * 添加值不为null的HTTP请求地址参数，允许同名的请求地址参数
     *
     * @param key   请求地址参数名称
     * @param value 请求地址参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    public DefaultRequest query(String key, Object value) {
        return query(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求地址参数，可选择对于同名的请求地址参数的处理方式
     *
     * @param key    请求地址参数名称
     * @param value  请求地址参数值，为null则不会被添加
     * @param append true：清除已经存在的同名的请求地址参数，false：追加同名的请求地址参数
     * @return HTTP请求实例
     */
    public DefaultRequest query(String key, Object value, boolean append) {
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
     * 添加值不为null的HTTP请求头，允许同名的请求头
     *
     * @param key   请求头名称
     * @param value 请求头值，为null则不会被添加
     * @return HTTP请求实例
     */
    public DefaultRequest header(String key, String value) {
        return header(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求头，可选择对于同名的请求头的处理方式
     *
     * @param key    请求头名称
     * @param value  请求头值，为null则不会被添加
     * @param append true：清除已经存在的同名的请求头，false：追加同名的请求头
     * @return HTTP请求实例
     */
    public DefaultRequest header(String key, String value, boolean append) {
        setHeader(key, value, append);
        return this;
    }

    /**
     * 添加值不为null的HTTP请求体参数，允许同名的请求体参数。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    public DefaultRequest content(String key, Object value) {
        return content(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求体参数，可选择对同名的请求体参数的处理方式。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key    请求体参数名
     * @param value  请求体参数值，为null则不会被添加
     * @param append true：清除已经存在的同名的请求体参数，false：追加同名请求体参数
     * @return HTTP请求实例
     */
    public DefaultRequest content(String key, Object value, boolean append) {
        Objects.requireNonNull(key);
        if (GET.equals(requestMethod) || DELETE.equals(requestMethod)) {
            throw new IllegalArgumentException(String.format("%s方法不能添加请求体", requestMethod));
        }
        if (this.requestContent == null) {
            this.requestContent = new DefaultRequest.UrlencodedContent();
        }
        if (this.requestContent instanceof XRequest.UrlencodedContent) {
            DefaultRequest.UrlencodedContent urlencodedContent = ((DefaultRequest.UrlencodedContent) this.requestContent);
            if (value instanceof File || value instanceof XRequest.MultipartContent.Part) {
                //如果请求体一开始是urlencoded类型的，现在来了一个文件，则自动转换成multipart类型的，然后交给multipart类型的处理逻辑处理
                DefaultRequest.MultipartContent multipartContent = new DefaultRequest.MultipartContent();
                for (KeyValue keyValue : urlencodedContent.params) {
                    multipartContent.part(keyValue.key, keyValue.value);
                }
                this.requestContent = multipartContent;
            } else {
                urlencodedContent.param(key, value, append);
                return this;
            }
        }
        if (this.requestContent instanceof DefaultRequest.MultipartContent) {
            DefaultRequest.MultipartContent multipartContent = (DefaultRequest.MultipartContent) this.requestContent;
            if (value instanceof DefaultRequest.MultipartContent.Part) {
                DefaultRequest.MultipartContent.Part part = (DefaultRequest.MultipartContent.Part) value;
                if (key.equals(part.name)) {
                    multipartContent.part((DefaultRequest.MultipartContent.Part) value, append);
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
     *
     * @param content 自定义的HTTP请求体
     * @return HTTP请求实例
     */
    public DefaultRequest content(Content content) {
        setContent(content);
        return this;
    }

    @Override
    public void setMethod(String method) {
        if (method.equals(GET) || method.equals(POST) || method.equals(DELETE) || method.equals(PUT)) {
            this.requestMethod = method;
        }
    }

    /**
     * 获得HTTP请求的请求方法
     *
     * @return HTTP请求的请求方法
     */
    @Override
    public String getMethod() {
        return this.requestMethod;
    }

    @Override
    public void setUrl(String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("仅支持HTTP和HTTPS协议");
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
     *
     * @return HTTP请求的请求url
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
     *
     * @return HTTP请求的请求头列表
     */
    @Override
    public List<KeyValue> getHeaders() {
        if ((this.requestMethod.equals(POST) || this.requestMethod.equals(PUT)) && this.requestContent != null) {
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
     *
     * @return HTTP请求的请求体
     */
    @Override
    public Content getContent() {
        return this.requestContent;
    }

    /**
     * urlencoded类型请求体
     */
    public static class UrlencodedContent implements Content {
        private final String charset = charset();
        private final List<KeyValue> params = new LinkedList<>();
        private byte[] urlencoded;

        public DefaultRequest.UrlencodedContent param(String key, Object value) {
            return this.param(key, value, false);
        }

        public DefaultRequest.UrlencodedContent param(String key, Object value, boolean clear) {
            Objects.requireNonNull(key);
            this.urlencoded = null;
            if (clear) {
                Iterator<KeyValue> iterator = this.params.iterator();
                while (iterator.hasNext()) {
                    KeyValue keyValue = iterator.next();
                    if (keyValue.key.equals(key)) {
                        iterator.remove();
                    }
                }
            }
            if (value != null) {
                this.params.add(new KeyValue(key, value));
            }
            return this;
        }

        public String charset() {
            return CHARSET_DEFAULT;
        }

        @Override
        public String contentType() {
            return MIME_URLENCODED + "; charset=" + charset;
        }

        @Override
        public long contentLength() throws IOException {
            if (urlencoded == null) {
                urlencoded = kvJoin(params, charset).getBytes(charset);
            }
            return urlencoded.length;
        }

        @Override
        public void contentWrite(OutputStream outStream) throws IOException {
            if (urlencoded == null) {
                urlencoded = kvJoin(params, charset).getBytes(charset);
            }
            outStream.write(urlencoded);
        }
    }

    /**
     * multipart类型请求体
     */
    public static class MultipartContent implements Content {
        public static final String HYPHENS = "--";
        public static final String CRLF = "\r\n";

        private final String charset = charset();
        private final List<DefaultRequest.MultipartContent.Part> parts = new LinkedList<>();
        private String boundary = StringUtils.md5(String.format("multipart-%d-%d", System.currentTimeMillis(), new Random().nextInt()));

        public DefaultRequest.MultipartContent part(String key, Object value) {
            return this.part(new DefaultRequest.MultipartContent.Part(key, value, charset), false);
        }

        public DefaultRequest.MultipartContent part(String key, Object value, boolean clear) {
            return this.part(new DefaultRequest.MultipartContent.Part(key, value, charset), clear);
        }

        public DefaultRequest.MultipartContent part(DefaultRequest.MultipartContent.Part part) {
            return this.part(part, false);
        }

        public DefaultRequest.MultipartContent part(DefaultRequest.MultipartContent.Part part, boolean clear) {
            Objects.requireNonNull(part);
            Objects.requireNonNull(part.name);
            if (clear) {
                Iterator<DefaultRequest.MultipartContent.Part> iterator = this.parts.iterator();
                while (iterator.hasNext()) {
                    DefaultRequest.MultipartContent.Part temp = iterator.next();
                    if (part.name.equals(temp.name)) {
                        iterator.remove();
                    }
                }
            }
            if (part.value != null) {
                this.parts.add(part);
            }
            return this;
        }

        public String charset() {
            return CHARSET_DEFAULT;
        }

        @Override
        public String contentType() {
            return MIME_MULTIPART + "; boundary=" + boundary;
        }

        @Override
        public long contentLength() throws IOException {
            long contentLength = 0;
            for (DefaultRequest.MultipartContent.Part part : parts) {
                contentLength += (HYPHENS + boundary + CRLF).getBytes(charset).length;
                for (String header : part.headers()) {
                    contentLength += String.format("%s%s", header, CRLF).getBytes(charset).length;
                }
                contentLength += CRLF.getBytes(charset).length;
                contentLength += part.partLength();
                contentLength += CRLF.getBytes(charset).length;
            }
            contentLength = contentLength + (HYPHENS + boundary + HYPHENS + CRLF).getBytes(charset).length;
            return contentLength;
        }

        @Override
        public void contentWrite(OutputStream outStream) throws IOException {
            for (DefaultRequest.MultipartContent.Part part : parts) {
                outStream.write((HYPHENS + boundary + CRLF).getBytes(charset));
                for (String header : part.headers()) {
                    outStream.write(String.format("%s%s", header, CRLF).getBytes(charset));
                }
                outStream.write(CRLF.getBytes(charset));
                part.partWrite(outStream);
                outStream.write(CRLF.getBytes(charset));
            }
            outStream.write((HYPHENS + boundary + HYPHENS + CRLF).getBytes(charset));
        }

        public static class Part {
            public final String name;
            public final Object value;
            public final String charset;

            public Part(String name, Object value) {
                this(name, value, CHARSET_DEFAULT);
            }

            public Part(String name, Object value, String charset) {
                this.name = name;
                this.value = value;
                this.charset = charset;
            }

            public String[] headers() throws IOException {
                if (value instanceof File) {
                    String disposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", name, URLEncoder.encode(((File) value).getName(), charset));
                    String type = String.format("Content-Type: %s", Files.probeContentType(Paths.get(((File) value).getAbsolutePath())));
                    return new String[]{disposition, type};
                } else {
                    return new String[]{String.format("Content-Disposition: form-data; name=\"%s\"", name)};
                }
            }

            public long partLength() throws IOException {
                if (value instanceof File) {
                    return ((File) value).length();
                } else {
                    return String.valueOf(value).getBytes(charset).length;
                }
            }

            public void partWrite(OutputStream outStream) throws IOException {
                if (value instanceof File) {
                    try (FileInputStream fiStream = new FileInputStream((File) value)) {
                        StringUtils.streamToStream(fiStream, outStream);
                    }
                } else {
                    outStream.write(String.valueOf(value).getBytes(charset));
                }
            }
        }
    }

    /**
     * 字符串类型请求体
     */
    public static class StringContent implements Content {
        private static Pattern P_CHARSET = Pattern.compile("charset\\s*=\\s*\"?(.+)\"?\\s*;?");

        public final String charset = charset();
        public final String mime;
        public final byte[] bytes;

        public StringContent(String mime, String str) {
            try {
                Matcher matcher = P_CHARSET.matcher(mime);
                if (matcher.find()) {
                    this.mime = mime;
                    this.bytes = str.getBytes(matcher.group(1));
                } else {
                    this.mime = mime + "; charset=" + charset;
                    this.bytes = str.getBytes(charset);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(String.format("无法将字符串以指定的编码方式【%s】进行编码", charset));
            }
        }

        public String charset() {
            return CHARSET_DEFAULT;
        }

        @Override
        public String contentType() {
            return mime;
        }

        @Override
        public long contentLength() {
            return bytes.length;
        }

        @Override
        public void contentWrite(OutputStream outStream) throws IOException {
            outStream.write(bytes);
        }
    }

    /**
     * 文件类型请求体
     */
    public static class FileContent implements Content {
        public final File file;

        public FileContent(File file) {
            this.file = file;
        }

        public String charset() {
            return CHARSET_DEFAULT;
        }

        @Override
        public String contentType() throws IOException {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        }

        @Override
        public long contentLength() {
            return file.length();
        }

        @Override
        public void contentWrite(OutputStream outStream) throws IOException {
            try (FileInputStream finStream = new FileInputStream(file)) {
                StringUtils.streamToStream(finStream, outStream);
            }
        }
    }
}
