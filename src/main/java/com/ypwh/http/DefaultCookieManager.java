package com.ypwh.http;

import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * cookie 管理器 默认接受所有cookie
 */
public class DefaultCookieManager extends CookieManager {

    public DefaultCookieManager() {
        super(null, CookiePolicy.ACCEPT_ALL);
    }
}
