package wechat;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 网页版微信api
 */
public class WeiChatApi {

    private final long timeInit = System.currentTimeMillis();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String[] HOSTS = {"wx.qq.com", "wx2.qq.com", "wx8.qq.com", "web.wechat.com", "web2.wechat.com"};
    private final AtomicBoolean firstLogin = new AtomicBoolean(true);


}
