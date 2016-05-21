package com.example.taikiy.pageapiapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Need to pop the history stack to disable "back" to this activity
        if (((PageApiApp)this.getApplication()).isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, NavigationDrawerActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, FacebookLoginActivity.class));
        }
    }
}
