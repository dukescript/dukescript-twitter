package com.dukescript.twitter;

import com.dukescript.twitterdemo.token.PlatformServices;
import net.java.html.boot.BrowserBuilder;
import org.robovm.apple.foundation.NSUserDefaults;

final class iOSMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(iOSMain.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }
    
    public static void onPageLoad() throws Exception {
        TwitterClient.onPageLoad(new iOSServices());
    }

    private static final class iOSServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return NSUserDefaults.getStandardUserDefaults().getString(key);
        }

        @Override
        public void setPreferences(String key, String value) {
            NSUserDefaults.getStandardUserDefaults().put(key, value);
        }
    }
    
}


