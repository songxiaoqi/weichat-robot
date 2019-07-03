package com.ypwh.robot.chat.commons.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.ypwh.robot.chat.commons.utils.STools;

public class Part {

    public final String name;
    public final Object value;
    public final String charset;

    public Part(String name, Object value) {
        this(name, value, STools.DEFAULT_CHARSET);
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
                STools.streamToStream(fiStream, outStream);
            }
        } else {
            outStream.write(String.valueOf(value).getBytes(charset));
        }
    }
}
