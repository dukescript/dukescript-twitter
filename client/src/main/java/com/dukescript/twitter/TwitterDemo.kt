@file:JvmName("Main")

package com.dukescript.twitter

import com.sun.corba.se.spi.orbutil.fsm.Action
import net.java.html.kotlin.Objs
import net.java.html.kotlin.action
import net.java.html.kotlin.actionWithData
import net.java.html.kotlin.computed
import net.java.html.kotlin.observable
import net.java.html.kotlin.observableList
import net.java.html.boot.BrowserBuilder
import net.java.html.json.Models

fun main(args: Array<String>) {
    BrowserBuilder.newBrowser().loadPage("pages/index.html")
            .loadFinished {
                onPageLoad(*args)
            }
            .showAndWait();
    System.exit(0);
}

fun onPageLoad(vararg args: String) {
        val BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAKOzBgAAAAAAdiww7KsRPsBd%2B%2FPJrEmVk8slQaU%3DTxNsLo3L82jXMA3ZeejrkDqMqTcrgQTj1xZLVdFtdPzkIXubWz";
        val model = TwitterDemo()
        model.savedLists.plus(arrayOf(
            Tweeters("API Design", "JaroslavTulach"),
            Tweeters("Celebrities", "JohnCleese", "MCHammer", "StephenFry", "algore", "StevenSanderson"),
            Tweeters("Microsoft people", "BillGates", "shanselman", "ScottGu"),
            Tweeters("NetBeans", "GeertjanW", "monacotoni", "NetBeans", "petrjiricka"),
            Tweeters("Tech pundits", "Scobleizer", "LeoLaporte", "techcrunch", "BoingBoing", "timoreilly", "codinghorror")
        ))
        model.activeTweetersName = "NetBeans";
        model.token = BEARER_TOKEN;
        Models.applyBindings(model);
        //model.refreshTweets();
}

class TwitterDemo : Objs.Provider {
    override val objs = Objs(this)
    val savedLists: MutableList<Tweeters> by observableList()
    var activeTweetersName by observable("")
    val activeTweeters: MutableList<String> by observableList()
    var userNameToAdd by observable("")
    val currentTweets: MutableList<Tweet> by observableList()
    var loading by observable(false)
    var token by observable("")

    val hasUnsavedChanges by computed {
        val tw = findByName(savedLists, activeTweetersName);
        tw.userNames != activeTweeters;
    }

    val activeTweetersCount by computed {
        activeTweeters.size;
    }

    val userNameToAddIsValid by computed {
        userNameToAdd.matches(Regex("[a-zA-Z0-9_]{1,15}")) && !activeTweeters.contains(userNameToAdd);
    }

    val deleteList by action {
        savedLists.remove(findByName(savedLists, activeTweetersName));
        if (savedLists.isEmpty()) {
            savedLists.add(Tweeters("New"));
        }
        activeTweetersName = savedLists[0].name;
    }

    val saveChanges by computed {
        val t = findByName(savedLists, activeTweetersName);
        val indx = savedLists.indexOf(t);
        if (indx != -1) {
            t.name = activeTweetersName
            t.userNames.clear()
            t.userNames += activeTweetersName
        }
    }

    val addUser by action {
        activeTweeters += userNameToAdd;
    }

    val removeUser by actionWithData { data : String? ->
        if (data != null) {
            activeTweeters -= data
        }
    }

    fun findByName(list : List<Tweeters>, name : String?): Tweeters {
        for (l in list) {
            if (l.name == name) {
                return l;
            }
        }
        return if (list.isEmpty()) Tweeters("New") else list.get(0);
    }

}

class Tweeters : Objs.Provider {
    override val objs = Objs(this)

    var name by observable("")
    val userNames: MutableList<String> by observableList()

    constructor (name : String, vararg userNames: String) {
        this.name = name
        this.userNames += userNames
    }
}

final class User : Objs.Provider {
    override val objs = Objs(this)

    var name by observable("")
    var id_str by observable("")
    var profile_image_url by observable("")

    val userUrl by computed {
        "http://twitter.com/${name}"
    }
}

final class Tweet : Objs.Provider {
    override val objs = Objs(this)
    var text: String by observable("")
    var created_at: String by observable("")
    var user: User? by observable(null)

    val html by computed {
        val sb = StringBuilder(320)
        var pos = 0
        while (true) {
            val http = text.indexOf("http", pos)
            if (http == -1) {
                sb.append(text.substring(pos))
                break
            }
            var spc = text.indexOf(' ', http);
            if (spc == -1) {
                spc = text.length;
            }
            sb.append(text.substring(pos, http));
            val url = text.substring (http, spc);
            sb.append("<a href='").append(url).append("'>").append(url).append("</a>");
            pos = spc;
        }
        sb.toString()
    }
}

/*
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
*/
