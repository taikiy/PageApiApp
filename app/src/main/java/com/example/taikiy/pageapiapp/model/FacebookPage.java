package com.example.taikiy.pageapiapp.model;

/**
 * Created by taikiy on 2016/05/19.
 */
public class FacebookPage {
    private long id;
    private String name;
    private String accessToken;
    private String category;

    public FacebookPage(long id, String name, String category, String accessToken) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.accessToken = accessToken;
    }

    public String getName() { return name; }
}
