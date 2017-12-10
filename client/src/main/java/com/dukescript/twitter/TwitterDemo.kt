@file:JvmName("Main")

package com.dukescript.twitter

import com.dukescript.api.kt.Model
import com.dukescript.api.kt.action
import com.dukescript.api.kt.actionWithData
import com.dukescript.api.kt.applyBindings
import com.dukescript.api.kt.computed
import com.dukescript.api.kt.observable
import com.dukescript.api.kt.observableList
import com.dukescript.api.kt.loadJSON
import net.java.html.boot.BrowserBuilder

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
    val model = TwitterDemo(BEARER_TOKEN, "NetBeans", Tweeters("API Design", "JaroslavTulach"),
            Tweeters("Celebrities", "JohnCleese", "MCHammer", "StephenFry", "algore", "StevenSanderson"),
            Tweeters("Microsoft people", "BillGates", "shanselman", "ScottGu"),
            Tweeters("NetBeans", "GeertjanW", "monacotoni", "NetBeans", "petrjiricka"),
            Tweeters("Tech pundits", "Scobleizer", "LeoLaporte", "techcrunch", "BoingBoing", "timoreilly", "codinghorror")
    )
    model.updateActiveTweeters()
    applyBindings(model);
}

private class TwitterDemo(
        token: String, selectedListName: String, vararg lists: Tweeters
) : Model.Provider {
    override val objs = Model(this)
    val savedLists: MutableList<Tweeters> by observableList(*lists) {
        updateActiveTweeters()
    }
    var activeTweetersName by observable(selectedListName) {
        updateActiveTweeters()
    }
    val activeTweeters: MutableList<String> by observableList() {
        refreshTweets()
    }
    var userNameToAdd by observable("")
    val currentTweets: MutableList<Tweet> by observableList()
    var loading by observable(false)
    var token by observable(token)

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

    val saveChanges by action {
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

    val removeUser by actionWithData { data: String? ->
        if (data != null) {
            activeTweeters -= data
        }
    }

    fun findByName(list: List<Tweeters>, name: String?): Tweeters {
        for (l in list) {
            if (l.name == name) {
                return l;
            }
        }
        return if (list.isEmpty()) Tweeters("New") else list.get(0);
    }

    fun refreshTweets() {
        if (!activeTweeters.isEmpty()) {
            var sb = "https://api.twitter.com/1.1/search"
            sb += "/tweets.json?"
            sb += "q="
            var sep = ""
            for (p in activeTweeters) {
                sb += sep
                sb += p
                sep = "%20OR%20"
            }
            loading = true

            loadJSON(sb, { data: List<TwitterQuery> -> Unit
                if (!data.isEmpty()) {
                    this.currentTweets.clear()
                    this.currentTweets.addAll(data[0].statuses)
                }
                loading = false
            }, headers = mapOf(
                Pair("Authorization", "Bearer ${token}")
            ))
        }
    }

    fun updateActiveTweeters() {
        val people = findByName(savedLists, activeTweetersName)
        activeTweeters.clear()
        activeTweeters.addAll(people.userNames)
    }
}

class Tweeters : Model.Provider {
    override val objs = Model(this)

    var name by observable("")
    val userNames: MutableList<String> by observableList()

    constructor (name: String, vararg userNames: String) {
        this.name = name
        this.userNames += userNames
    }
}

final class User : Model.Provider {
    override val objs = Model(this)

    var name by observable("")
    var id_str by observable("")
    var profile_image_url by observable("")

    val userUrl by computed {
        "http://twitter.com/${name}"
    }
}

final class Tweet : Model.Provider {
    override val objs = Model(this)
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
            val url = text.substring(http, spc);
            sb.append("<a href='").append(url).append("'>").append(url).append("</a>");
            pos = spc;
        }
        sb.toString()
    }
}

class TwitterQuery : Model.Provider {
    override val objs = Model(this)
    
    val statuses: MutableList<Tweet> by observableList()
}

