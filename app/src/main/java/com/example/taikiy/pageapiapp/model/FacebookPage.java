package com.example.taikiy.pageapiapp.model;

/**
 * Created by taikiy on 2016/05/19.
 */
public class FacebookPage {
    private String id;
    private int menuItemId;
    private String name;
    private String accessToken;
    private String category;

    public FacebookPage(String id, String name, String category, String accessToken) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.accessToken = accessToken;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessToken() { return this.accessToken; }

    public void setMenuItemId(int index) {
        this.menuItemId = index;
    }

    public int getMenuItemId() {
        return this.menuItemId;
    }
}
