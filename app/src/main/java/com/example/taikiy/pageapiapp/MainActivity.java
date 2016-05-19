package com.example.taikiy.pageapiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taikiy.pageapiapp.model.FacebookPage;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String PREFERENCES_FILE_NAME = "PreferencesFile";
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);

        // Facebook magics
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);

        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken == null) {
            startActivity(new Intent(MainActivity.this, FacebookLoginActivity.class));
        } else {
            setContentView(R.layout.activity_main);

            updatePageListAsync();

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
    }

    private void updatePageListAsync() {
        GraphRequestAsyncTask task = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/accounts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        ArrayList<FacebookPage> list = new ArrayList<>();
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject page = data.getJSONObject(i);
                                long id = page.getLong("id");
                                String name = page.getString("name");
                                String category = page.getString("category");
                                String accessToken = page.getString("access_token");
                                list.add(new FacebookPage(id, name, category, accessToken));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView view = (TextView)findViewById(R.id.main_text_view);
                        String names = "";
                        for (FacebookPage page : list) {
                            names += page.getName();
                        }
                        view.setText(names);
                    }
                }
        ).executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
