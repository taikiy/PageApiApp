package com.example.taikiy.pageapiapp;

import android.app.Application;
import android.content.SharedPreferences;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by taikiy on 2016/05/21.
 */
public class PageApiApp extends Application {
    private static final String PREFERENCES_FILE_NAME = "PreferencesFile";

    @Override
    public void onCreate() {
        super.onCreate();

        // Load preferences
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
            return false;
        return true;
    }

    public boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        for (String permission : accessToken.getPermissions()) {
            if (permission.equals("publish_pages"))
                return true;
        }
        return false;
    }

    public AccessToken getAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }
}
