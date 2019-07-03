package com.ypwh.robot.chat.commons.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.ypwh.robot.chat.commons.http.Content;
import com.ypwh.robot.chat.commons.utils.STools;

/**
 * multipart类型请求
 */
public class MultipartContent implements Content {

    public static final String HYPHENS = "--";
    public static final String CRLF = "\r\n";

    private final String charset = charset();
    private final List<Part> parts = new LinkedList<>();
    private String boundary = STools.md5(String.format("multipart-%d-%d", System.currentTimeMillis(), new Random().nextInt()));

    public MultipartContent part(String key, Object value) {
        return this.part(new Part(key, value, charset), false);
    }

    public MultipartContent part(String key, Object value, boolean clear) {
        return this.part(new Part(key, value, charset), clear);
    }

    public MultipartContent part(Part part) {
        return this.part(part, false);
    }

    public MultipartContent part(Part part, boolean clear) {
        Objects.requireNonNull(part);
        Objects.requireNonNull(part.name);
        if (clear) {
            Iterator<Part> iterator = this.parts.iterator();
            while (iterator.hasNext()) {
                Part temp = iterator.next();
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
        return STools.DEFAULT_CHARSET;
    }

    @Override
    public String contentType() {
        return DefaultRequest.MIME_MULTIPART + "; boundary=" + boundary;
    }

    @Override
    public long contentLength() throws IOException {
        long contentLength = 0;
        for (Part part : parts) {
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
        for (Part part : parts) {
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

}
