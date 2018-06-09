package com.quickliftpilot.model;

/**
 * Created by amit on 1/4/18.
 */

/***
 * To get the rider history
 */

public class RideHistory {

    String date;
    Feed feed;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }
}
