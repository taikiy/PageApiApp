package com.example.taikiy.pageapiapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.taikiy.pageapiapp.model.FacebookPage;
import com.example.taikiy.pageapiapp.model.FacebookPost;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageDetailActivity extends AppCompatActivity {
    private List<FacebookPost> facebookPostList;
    //TODO: implement serializable in FacebookPage and pass the instance
    private String facebookPageId;
    private String facebookPageName;
    private String facebookPageAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Facebook magics
        //FacebookSdk.sdkInitialize(this.getApplicationContext());
        //callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_page_detail);

        facebookPageId = getIntent().getStringExtra("pageId");
        facebookPageName = getIntent().getStringExtra("pageName");
        facebookPageAccessToken = getIntent().getStringExtra("pageAccessToken");
        setTitle(facebookPageName);

        updateContentAsync();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PageDetailActivity.this, CreatePostActivity.class);
                intent.putExtra("pageId", facebookPageId);
                intent.putExtra("pageName", facebookPageName);
                intent.putExtra("pageAccessToken", facebookPageAccessToken);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateContentAsync() {
        //TODO: diff update
        facebookPostList = new ArrayList<>();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/feed", facebookPageId),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject page = data.getJSONObject(i);
                                String id = page.getString("id");
                                String message = page.getString("message");
                                String createdTime = page.getString("created_time");
                                facebookPostList.add(new FacebookPost(id, message, createdTime));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        updateFacebookPostItems();
                    }
                }
        ).executeAsync();
    }

    private void updateFacebookPostItems() {
        //TODO: cache and only reload when explicitly commanded
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_page_detail_linearlayout);
        for (FacebookPost post : facebookPostList) {
            //LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //View item = inflater.inflate(R.layout.post_item_tile, null);
            PostItemTile tile = new PostItemTile(this);
            tile.setContent(post);
            updatePostDetail(post, "is_published", (TextView)tile.findViewById(R.id.post_item_publish_status));
            updatePostInsight(post, "post_impressions_unique", (TextView)tile.findViewById(R.id.post_item_view_count));
            layout.addView(tile);
        }
    }

    private void updatePostDetail(final FacebookPost post,
                                  final String fieldName,
                                  final TextView view) {
        Bundle bundle = new Bundle();
        bundle.putString("fields", fieldName);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s", post.getId()),
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject json = response.getJSONObject();
                            String value = json.getString(fieldName);
                            post.setInsight(fieldName, value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //TextView view = (TextView)tile.findViewById(R.id.
                        view.setText(post.getPublishStatus());
                    }
                }
        ).executeAsync();
    }

    private void updatePostInsight(final FacebookPost post,
                                   final String insightName,
                                   //final PostItemTile tile) {
                                   final TextView view) {
        //TODO: permission check - "read_insights"
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/insights/%s", post.getId(), insightName),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            JSONObject page = data.getJSONObject(0);
                            String name = page.getString("name");
                            String value = page.getJSONArray("values").getJSONObject(0).getString("value");
                            post.setInsight(name, value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //TextView view = (TextView)tile.findViewById(R.id.post_item_view_count);
                        view.setText(String.valueOf(post.getViewCount()));
                    }
                }
        ).executeAsync();
    }
}
