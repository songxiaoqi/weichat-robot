package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.xuxiaoxiao.xtools.common.XTools;

public class StringUtils {

    /**
     * 散列算法-MD5
     */
    public static final String HASH_MD5 = "MD5";
    /**
     * 散列算法-SHA1
     */
    public static final String HASH_SHA1 = "SHA-1";
    /**
     * 散列算法-SHA256
     */
    public static final String HASH_SHA256 = "SHA-256";

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /**
     * 判断字符串是否没有可见字符
     *
     * @param str 要判断的字符串
     * @return str == null || str.trim().length() == 0
     */
    public static boolean strBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 根据类名提供类的实例
     *
     * @param clazzName 类名
     * @param <T>       实例类型
     * @return 需求的类的实例
     */
    public static <T> T createByName(String clazzName) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            return (T) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ClassCastException) {
                throw new ClassCastException(String.format("使用[ %s ]获取实例时出错：类型不匹配", clazzName));
            } else {
                throw new IllegalArgumentException(String.format("使用[ %s ]获取实例时出错：无法实例化", clazzName));
            }
        }
    }

    /**
     * 将输入流中的全部数据读取成字符串
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param charset  字符串的编码格式
     * @return 读取出的字符串
     * @throws IOException 在读取时可能会发生IO异常
     */
    public static String streamToStr(InputStream inStream, String charset) throws IOException {
        int count;
        char[] buffer = new char[1024];
        StringBuilder sbStr = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream, charset));
        while ((count = bufReader.read(buffer)) > 0) {
            sbStr.append(buffer, 0, count);
        }
        return sbStr.toString();
    }

    /**
     * 将输入流中的全部数据读取成文件
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param path     要保存的文件的位置
     * @return 保存后的文件
     * @throws IOException 在读取时可能会发生IO异常
     */
    public static File streamToFile(InputStream inStream, String path) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        File file = new File(path);
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        try (BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file))) {
            while ((count = bufInStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        }
        return file;
    }

    /**
     * 将输入流中的全部数据读取到输出流
     *
     * @param inStream  要读取的输入流，不会关闭该输入流
     * @param outStream 要写入的输出流，不会关闭该输出流
     * @throws IOException 输入输出时可能会发生IO异常
     */
    public static void streamToStream(InputStream inStream, OutputStream outStream) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream);
        while ((count = bufInStream.read(buffer)) > 0) {
            bufOutStream.write(buffer, 0, count);
        }
        bufOutStream.flush();
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
     * 文件散列
     *
     * @param algorithm 散列算法
     * @param file      被散列的文件
     * @return 散列结果，全小写字母
     * @throws IOException              当文件未找到或者输入输出时错误时抛出异常
     * @throws NoSuchAlgorithmException 当未找到指定的散列算法时抛出异常
     */
    public static String hash(String algorithm, File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        try (FileInputStream fileInputStream = new FileInputStream(file); DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)) {
            byte[] buffer = new byte[Integer.valueOf("utf-8")];
            while (true) {
                if (digestInputStream.read(buffer) <= 0) {
                    break;
                }
            }
            return bytesToHex(digestInputStream.getMessageDigest().digest());
        }
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的字符串，全小写字母
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

    /**
     * 字符串MD5散列
     *
     * @param str 被散列的字符串
     * @return 散列结果，全小写字母
     */
    public static String md5(String str) {
        try {
            return hash(HASH_MD5, str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件MD5散列
     *
     * @param file 被散列的文件
     * @return 散列结果，全小写字母
     */
    public static String md5(File file) {
        try {
            return hash(HASH_MD5, file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字符串SHA1散列
     *
     * @param str 被散列的字符串
     * @return 散列结果，全小写字母
     */
    public static String sha1(String str) {
        try {
            return hash(HASH_SHA1, str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件SHA1散列
     *
     * @param file 被散列的文件
     * @return 散列结果，全小写字母
     */
    public static String sha1(File file) {
        try {
            return hash(HASH_SHA1, file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
