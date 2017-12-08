package com.dukescript.twitter;

import android.app.Activity;
import android.content.SharedPreferences;
import com.dukescript.twitterdemo.token.PlatformServices;

public class AndroidMain extends Activity {
    private AndroidMain() {
    }

    public static void main(android.content.Context context) throws Exception {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(AndroidMain.class.getPackage().getName(), 0);
        TwitterClient.onPageLoad(new AndroidServices(prefs));
    }

    private static final class AndroidServices extends PlatformServices {
        private final SharedPreferences prefs;

        AndroidServices(SharedPreferences prefs) {
            this.prefs = prefs;
        }
        @Override
        public String getPreferences(String key) {
            return prefs.getString(key, null);
        }

        @Override
        public void setPreferences(String key, String value) {
            prefs.edit().putString(key, value).apply();
        }
    }
}