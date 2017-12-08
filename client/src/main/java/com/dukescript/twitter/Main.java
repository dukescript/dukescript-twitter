/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2015 Anton Epple <anton.epple@eppleton.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dukescript.twitter;

import com.dukescript.twitterdemo.token.PlatformServices;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import net.java.html.boot.BrowserBuilder;


/**
 * Boots browser and then performs initialization when the HTML page is ready.
 *
 * @author Anton Epple <anton.epple@eppleton.de>
 */
public class Main {


    private Main() {
    }

    public static void main(String... args) {
        BrowserBuilder.newBrowser().loadPage("pages/index.html")
                .invoke("onPageLoad", args).loadClass(Main.class)
                .showAndWait();
        System.exit(0);
        
    }
    
    public static void onPageLoad(PlatformServices services) throws Exception {
          try {  
            TwitterClient.onPageLoad(services);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void onPageLoad() throws Exception {
        // don't put "common" initialization stuff here, other platforms (iOS, Android, Bck2Brwsr) may not call this method. They rather call DataModel.onPageLoad
        onPageLoad(new DesktopServices());
    }

    private static final class DesktopServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return Preferences.userNodeForPackage(Main.class).get(key, null);
        }

        @Override
        public void setPreferences(String key, String value) {
            Preferences.userNodeForPackage(Main.class).put(key, value);
        }
    }


}
