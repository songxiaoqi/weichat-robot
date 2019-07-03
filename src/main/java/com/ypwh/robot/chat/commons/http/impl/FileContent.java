package com.ypwh.robot.chat.commons.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.ypwh.robot.chat.commons.http.Content;
import com.ypwh.robot.chat.commons.utils.STools;

/**
 * 文件请求类型
 */
public class FileContent implements Content {

    public final File file;

    public FileContent(File file) {
        this.file = file;
    }

    public String charset() {
        return STools.DEFAULT_CHARSET;
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
            STools.streamToStream(finStream, outStream);
        }
    }
}
