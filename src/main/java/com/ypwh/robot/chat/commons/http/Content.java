package com.ypwh.robot.chat.commons.http;

import java.io.IOException;
import java.io.OutputStream;

public interface Content {

    /**
     * 请求体的MIME类型
     *
     */
    String contentType() throws IOException;

    /**
     * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
     *
     */
    long contentLength() throws IOException;

    /**
     * 请求体写出到输出流的具体方法
     *
     */
    void contentWrite(OutputStream outStream) throws IOException;
}
