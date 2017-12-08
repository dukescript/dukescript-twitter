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

import com.dukescript.twitterdemo.token.BearerTokenUtil;
import com.dukescript.twitterdemo.token.PlatformServices;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/**
 * Controller class for access to Twitter.
 *
 * @author Anton Epple <anton.epple@eppleton.de>
 */
@Model(className = "TwitterModel", builder = "put", properties = {
    @Property(name = "credentials", type = Credentials.class),
    @Property(name = "savedLists", type = Tweeters.class, array = true),
    @Property(name = "activeTweetersName", type = String.class),
    @Property(name = "activeTweeters", type = String.class, array = true),
    @Property(name = "userNameToAdd", type = String.class),
    @Property(name = "currentTweets", type = Tweet.class, array = true),
    @Property(name = "loading", type = boolean.class),
    @Property(name = "token", type = String.class),
}, instance = true, targetId = "")
final class TwitterClient {
    
    PlatformServices services;
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    
    @ModelOperation
    public void setPreferences(TwitterModel model, PlatformServices services){
        this.services = services;
    }
    
    @Model(className = "Credentials", properties = {
        @Property(name = "key", type = String.class),
        @Property(name = "secret", type = String.class)
    })
    static class CredentialsVM {
    }
        
    @Model(className = "Tweeters", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "userNames", type = String.class, array = true)
    })
    static class Twttrs {
    }

    @Model(className = "User", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "id_str", type = int.class),
        @Property(name = "profile_image_url", type = String.class),})
    static final class Sr {

        @ComputedProperty
        static String userUrl(String name) {
            return "http://twitter.com/" + name;
        }
    }

    @Model(className = "Tweet", properties = {
        @Property(name = "text", type = String.class),
        @Property(name = "created_at", type = String.class),
        @Property(name = "user", type = User.class)
    })
    static final class Twt {

        @ComputedProperty
        static String html(String text) {
            StringBuilder sb = new StringBuilder(320);
            for (int pos = 0;;) {
                int http = text.indexOf("http", pos);
                if (http == -1) {
                    sb.append(text.substring(pos));
                    return sb.toString();
                }
                int spc = text.indexOf(' ', http);
                if (spc == -1) {
                    spc = text.length();
                }
                sb.append(text.substring(pos, http));
                String url = text.substring(http, spc);
                sb.append("<a href='").append(url).append("'>").append(url).append("</a>");
                pos = spc;
            }
        }

    }

    @Model(className = "TwitterQuery", properties = {
        @Property(array = true, name = "statuses", type = Twt.class)
    })
    public static final class TwttrQr {
    }

    @OnReceive(headers = {
        "Authorization: Bearer {token}"
    }, url = "{root}/tweets.json?{query}")
    static void queryTweets(TwitterModel page, TwitterQuery q) {
        page.getCurrentTweets().clear();
        page.getCurrentTweets().addAll(q.getStatuses());
        page.setLoading(false);
    }

    @OnPropertyChange("activeTweetersName")
    static void changeTweetersList(TwitterModel model) {
        Tweeters people = findByName(model.getSavedLists(), model.getActiveTweetersName());
        model.getActiveTweeters().clear();
        model.getActiveTweeters().addAll(people.getUserNames());
    }

    @ModelOperation
    @OnPropertyChange({"activeTweeters", "activeTweetersCount"})
    static void refreshTweets(TwitterModel model) {
        if (model.getActiveTweeters().isEmpty()) {
            return;
        }
        if (model.getToken() == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("q=");
        String sep = "";
        for (String p : model.getActiveTweeters()) {
            sb.append(sep);
            sb.append(p);
            sep = "%20OR%20";
        }

        model.setLoading(true);
        model.queryTweets("https://api.twitter.com/1.1/search", sb.toString(), model.getToken());
    }

    static void onPageLoad(PlatformServices services) throws IOException {
//          services.setPreferences(BEARER_TOKEN, "");
          final String bearerToken = services.getPreferences(BEARER_TOKEN);

         final TwitterModel model = new TwitterModel().putSavedLists(
            new Tweeters("API Design", "JaroslavTulach"),
            new Tweeters("Celebrities", "JohnCleese", "MCHammer", "StephenFry", "algore", "StevenSanderson"),
            new Tweeters("Microsoft people", "BillGates", "shanselman", "ScottGu"),
            new Tweeters("NetBeans", "GeertjanW", "monacotoni", "NetBeans", "petrjiricka"),
            new Tweeters("Tech pundits", "Scobleizer", "LeoLaporte", "techcrunch", "BoingBoing", "timoreilly", "codinghorror")
        ).putActiveTweetersName("NetBeans").putToken(bearerToken).putCredentials(new Credentials());
        model.setPreferences(services);
        model.applyBindings();
        model.refreshTweets();
    }
    
    @Function
    public void getBearerToken(TwitterModel model, Credentials data){
        try {
            String bearerToken = BearerTokenUtil.getBearerToken(data.getKey(), data.getSecret());
            if (bearerToken!=null &! bearerToken.isEmpty()){
                model.setToken(bearerToken);
                services.setPreferences(BEARER_TOKEN, bearerToken);
                model.refreshTweets();
            }
        } catch (IOException ex) {
            Logger.getLogger(TwitterClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @ComputedProperty
    static boolean hasUnsavedChanges(List<String> activeTweeters, List<Tweeters> savedLists, String activeTweetersName) {
        Tweeters tw = findByName(savedLists, activeTweetersName);
        if (activeTweeters == null) {
            return false;
        }
        return !tw.getUserNames().equals(activeTweeters);
    }

    @ComputedProperty
    static int activeTweetersCount(List<String> activeTweeters) {
        return activeTweeters.size();
    }

    @ComputedProperty
    static boolean userNameToAddIsValid(
            String userNameToAdd, String activeTweetersName, List<Tweeters> savedLists, List<String> activeTweeters
    ) {
        return userNameToAdd != null
                && userNameToAdd.matches("[a-zA-Z0-9_]{1,15}")
                && !activeTweeters.contains(userNameToAdd);
    }

    @Function
    static void deleteList(TwitterModel model) {
        final List<Tweeters> sl = model.getSavedLists();
        sl.remove(findByName(sl, model.getActiveTweetersName()));
        if (sl.isEmpty()) {
            sl.add(new Tweeters("New"));
        }
        model.setActiveTweetersName(sl.get(0).getName());
    }

    @Function
    static void saveChanges(TwitterModel model) {
        Tweeters t = findByName(model.getSavedLists(), model.getActiveTweetersName());
        int indx = model.getSavedLists().indexOf(t);
        if (indx != -1) {
            t.setName(model.getActiveTweetersName());
            t.getUserNames().clear();
            t.getUserNames().addAll(model.getActiveTweeters());
        }
    }

    @Function
    static void addUser(TwitterModel model) {
        String n = model.getUserNameToAdd();
        model.getActiveTweeters().add(n);
    }

    @Function
    static void removeUser(String data, TwitterModel model) {
        model.getActiveTweeters().remove(data);
    }

    private static Tweeters findByName(List<Tweeters> list, String name) {
        for (Tweeters l : list) {
            if (l.getName() != null && l.getName().equals(name)) {
                return l;
            }
        }
        return list.isEmpty() ? new Tweeters() : list.get(0);
    }
}
