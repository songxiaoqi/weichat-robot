package com.ypwh.robot.chat.commons.http.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ypwh.robot.chat.commons.http.Content;
import com.ypwh.robot.chat.commons.utils.STools;

/**
 * 字符串请求类型
 */
public class StringContent implements Content {

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
        return STools.DEFAULT_CHARSET;
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
