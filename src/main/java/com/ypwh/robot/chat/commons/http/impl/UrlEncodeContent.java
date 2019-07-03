package com.ypwh.robot.chat.commons.http.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.ypwh.robot.chat.commons.http.Content;
import com.ypwh.robot.chat.commons.http.KeyValue;
import com.ypwh.robot.chat.commons.utils.STools;

/**
 * url编码类型
 */
public class UrlEncodeContent implements Content {

    private final String charset = charset();
    private final List<KeyValue> params = new LinkedList<>();
    private byte[] urlencoded;

    public List<KeyValue> getParams() {
        return params;
    }

    public UrlEncodeContent param(String key, Object value) {
        return this.param(key, value, false);
    }

    public UrlEncodeContent param(String key, Object value, boolean clear) {
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
        return STools.DEFAULT_CHARSET;
    }

    @Override
    public String contentType() {
        return DefaultRequest.MIME_URLENCODED + "; charset=" + charset;
    }

    @Override
    public long contentLength() throws IOException {
        if (urlencoded == null) {
            urlencoded = DefaultRequest.kvJoin(params, charset).getBytes(charset);
        }
        return urlencoded.length;
    }

    @Override
    public void contentWrite(OutputStream outStream) throws IOException {
        if (urlencoded == null) {
            urlencoded = DefaultRequest.kvJoin(params, charset).getBytes(charset);
        }
        outStream.write(urlencoded);
    }
}
