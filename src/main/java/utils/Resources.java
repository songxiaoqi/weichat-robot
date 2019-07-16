package utils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Resources {
    private static Log log = LogFactory.getLog(Resources.class);
    public static ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    public Resources() {
    }

    public static void close() {
        resourceBundle = null;
    }

    public static String myString() {
        return resourceBundle.toString();
    }

    public static String getString(String key) {
        try {
            return !resourceBundle.containsKey(key) ? "" : resourceBundle.getString(key);
        } catch (MissingResourceException var2) {
            log.error(var2);
            var2.printStackTrace();
            return "";
        } catch (NullPointerException var3) {
            log.error(var3);
            return "";
        }
    }

    public static int getConfigAsInt(String key) {
        String res = getString(key);
        return Integer.valueOf(NuNString(res) ? "-1" : res);
    }

    public static String getString(String key, Object[] args) {
        try {
            return MessageFormat.format(getString(key), args);
        } catch (MissingResourceException var3) {
            log.error(var3);
            var3.printStackTrace();
            return "";
        } catch (NullPointerException var4) {
            log.error(var4);
            return "";
        }
    }


    public static boolean NuNString(String src) {
        return src == null || src.trim().isEmpty();
    }

}
