package com.ypwh.http.impl;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.*;
import utils.StringUtils;
import com.ypwh.http.*;

/**
 * http执行器默认实现
 */
public class DefaultHttpExecutor implements HttpExecutor {

    public static final int CONNECT_TIMEOUT_DEFAULT = 20000;
    public static final int READ_TIMEOUT_DEFAULT = 60000;
    public static final int CHUNK_LENGTH_DEFAULT = 262144;
    public static final boolean FOLLOW_REDIRECT_DEFAULT = false;

    public static final String SSL_ALGORITHM_DEFAULT = "TLS";
    public static final String SSL_KEY_MANAGERS_DEFAULT = "";
    public static final String SSL_TRUST_MANAGERS_DEFAULT = "";
    public static final String SSL_SECURE_RANDOM_DEFAULT = "";

    private int connectTimeout;
    private int readTimeout;
    private int chunkLength;
    private boolean followRedirect;
    private CookieManager cookieManager;
    private HostnameVerifier hostnameVerifier;
    private Interceptor[] interceptors;
    private SSLContext sslContext;

    public DefaultHttpExecutor(){
        this.connectTimeout=CONNECT_TIMEOUT_DEFAULT;
        this.readTimeout=READ_TIMEOUT_DEFAULT;
        this.chunkLength=CHUNK_LENGTH_DEFAULT;
        this.followRedirect=FOLLOW_REDIRECT_DEFAULT;
        this.cookieManager=new DefaultCookieManager();
        this.hostnameVerifier= new MyHostnameVerifier();
        this.interceptors = new Interceptor[0];
        defaultSSL();
    }

    @Override
    public DefaultResponse execute(Request request) throws Exception {
        HttpURLConnection connection = connect(request);
        connection.setRequestMethod(request.getMethod());

        List<KeyValue> headers = request.getHeaders();
        if (headers != null) {//请求头
            for (KeyValue keyValue : headers) {
                connection.addRequestProperty(keyValue.key, String.valueOf(keyValue.value));
            }
        }

        if (cookieManager != null) {//添加cookie信息
            Map<String, List<String>> cookiesList = cookieManager.get(connection.getURL().toURI(), new HashMap<String, List<String>>());
            for (String cookieType : cookiesList.keySet()) {
                StringBuilder sbCookie = new StringBuilder();
                for (String cookieStr : cookiesList.get(cookieType)) {
                    if (sbCookie.length() > 0) {
                        sbCookie.append(';');
                    }
                    sbCookie.append(cookieStr);
                }
                if (sbCookie.length() > 0) {
                    connection.setRequestProperty(cookieType, sbCookie.toString());
                }
            }
        }

        //如果为POST或PUT方法则输出请求体
        if (DefaultRequest.POST.equals(request.getMethod()) || DefaultRequest.PUT.equals(request.getMethod())) {
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            Content content = request.getContent();
            if (content != null) {
                if (content.contentLength() < 0) {
                    connection.setChunkedStreamingMode(getChunkLength());
                }
                try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                    content.contentWrite(dOutStream);
                }
            }
        }
        //获取输入流
        InputStream inStream = connection.getInputStream();
        if (cookieManager != null) {
            //读取cookie
            cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
        }
        return new DefaultResponse(connection, inStream);
    }

    protected HttpURLConnection connect(Request request) throws Exception {
        String url = request.getUrl();
        if (url.toLowerCase().startsWith("http://")) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(getConnectTimeout());
            connection.setReadTimeout(getReadTimeout());
            connection.setInstanceFollowRedirects(getFollowRedirect());
            return connection;
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(getSslContext().getSocketFactory());
            connection.setHostnameVerifier(getHostnameVerifier());
            connection.setConnectTimeout(getConnectTimeout());
            connection.setReadTimeout(getReadTimeout());
            connection.setInstanceFollowRedirects(getFollowRedirect());
            return connection;
        } else {
            throw new IllegalArgumentException("仅支持HTTP和HTTPS协议");
        }
    }

    private void defaultSSL() {
        try {
            this.sslContext = SSLContext.getInstance(SSL_ALGORITHM_DEFAULT);

            KeyManager[] keyManagers = null;
            String keyManagersStr = SSL_KEY_MANAGERS_DEFAULT;
            if (!StringUtils.strBlank(keyManagersStr)) {
                String[] array = keyManagersStr.split(",");
                keyManagers = new KeyManager[array.length];
                for (int i = 0, len = array.length; i < len; i++) {
                    keyManagers[i] = StringUtils.createByName(array[i].trim());
                }
            }

            TrustManager[] trustManagers = null;
            String trustManagersStr = SSL_TRUST_MANAGERS_DEFAULT;
            if (!StringUtils.strBlank(trustManagersStr)) {
                String[] array = trustManagersStr.split(",");
                trustManagers = new TrustManager[array.length];
                for (int i = 0, len = array.length; i < len; i++) {
                    trustManagers[i] = StringUtils.createByName(array[i].trim());
                }
            }

            SecureRandom secureRandom = null;
            String secureRandomStr = SSL_SECURE_RANDOM_DEFAULT;
            if (!StringUtils.strBlank(secureRandomStr)) {
                secureRandom = StringUtils.createByName(secureRandomStr);
            }

            sslContext.init(keyManagers, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setInterceptors(Interceptor... interceptor) {
        this.interceptors = interceptor;
    }

    @Override
    public Interceptor[] getInterceptors() {
        return interceptors;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void addCookie(URI uri, HttpCookie cookie) {
        this.cookieManager.getCookieStore().add(uri,cookie);
    }

    @Override
    public List<HttpCookie> getCookies(URI uri) {
        return this.cookieManager.getCookieStore().get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return this.cookieManager.getCookieStore().getCookies();
    }

    @Override
    public void rmCookie(URI uri, HttpCookie cookie) {
        this.cookieManager.getCookieStore().remove(uri,cookie);
    }

    @Override
    public void rmAllCookies() {
        this.cookieManager.getCookieStore().removeAll();
    }

    /**
     * get set--------------------------------------------------------------
     */
    public int getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(int chunkLength) {
        this.chunkLength = chunkLength;
    }

    public boolean getFollowRedirect() {
        return this.followRedirect;
    }

    public void setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void setCookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * 默认的主机名验证器，不进行主机名校验
     */
    public static class MyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }


    public static void main(String[] args) throws Exception {
        DefaultHttpExecutor executor = new DefaultHttpExecutor();
        DefaultRequest defaultRequest = new DefaultRequest("GET","https://www.baidu.com");
        DefaultResponse execute = executor.execute(defaultRequest);
        System.out.println(execute.string("utf-8"));
    }
}
