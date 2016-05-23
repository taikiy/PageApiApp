package com.example.taikiy.pageapiapp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by taikiy on 2016/05/21.
 */
public class FacebookPost {
    private String id;
    private String message;
    private Date createdTime;
    private int viewCount;
    private HashMap<String, String> insights;

    public FacebookPost(String id, String message, String createdTimeString) {
        this.id = id;
        this.message = message;
        this.viewCount = 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            this.createdTime = format.parse(createdTimeString);
        } catch (ParseException e) {
            //TODO: error handle
            e.printStackTrace();
        }
        this.insights = new HashMap<>();
    }

    public String getId() { return this.id; }

    public String getMessage() {
        return this.message;
    }

    public String getCreatedTimeString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(this.createdTime) + " at " + timeFormat.format(this.createdTime);
    }

    public int getViewCount() {
        if (this.insights.containsKey("post_impressions_unique")) {
            this.viewCount = Integer.parseInt(this.insights.get("post_impressions_unique"));
        }
        return this.viewCount;
    }

    public boolean isPublished() {
        if (this.insights.containsKey("is_published")) {
            boolean isPublished = Boolean.parseBoolean(this.insights.get("is_published"));
            return isPublished;
        }
        return true;
    }

    public String getPublishStatus() {
        if (this.isPublished())
            return "Published on ";
        else
            return "Will be published on ";
    }

    public void setInsight(String key, String value) {
        this.insights.put(key, value);
    }
}
