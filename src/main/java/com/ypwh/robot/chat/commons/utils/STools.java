package com.ypwh.robot.chat.commons.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 工具类
 */
public class STools {

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String HASH_MD5 = "MD5";
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public static void streamToStream(FileInputStream inStream, OutputStream outStream)throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream);
        while ((count = bufInStream.read(buffer)) > 0) {
            bufOutStream.write(buffer, 0, count);
        }
        bufOutStream.flush();
    }

    public static String md5(String format) {
        try {
            return hash(HASH_MD5, format.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 字节数组散列
     *
     * @param algorithm 散列算法
     * @param bytes     被散列的字节数组
     * @return 散列结果，全小写字母
     * @throws NoSuchAlgorithmException 当未找到指定的散列算法时抛出异常
     */
    public static String hash(String algorithm, byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    /**
     * 将字节数组转换成16进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX[b & 0xf];
        }
        return new String(chars);
    }
}
