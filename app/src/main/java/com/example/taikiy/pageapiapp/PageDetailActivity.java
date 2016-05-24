package com.example.taikiy.pageapiapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.taikiy.pageapiapp.model.FacebookPost;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageDetailActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private List<FacebookPost> facebookPostList;
    //TODO: implement serializable in FacebookPage and pass the instance
    private String facebookPageId;
    private String facebookPageName;
    private String facebookPageAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_detail);

        facebookPageId = getIntent().getStringExtra("pageId");
        facebookPageName = getIntent().getStringExtra("pageName");
        facebookPageAccessToken = getIntent().getStringExtra("pageAccessToken");
        setTitle(facebookPageName);

        //updateContent();

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

    @Override
    protected void onResume() {
        super.onResume();
        updateContent();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateContent() {
        // Permission check
        final PageApiApp app = (PageApiApp)this.getApplication();
        if (!app.hasManagePermission()) {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    //permission check - reading posts requires "manage_pages"
                    if (app.hasManagePermission()) {
                        getPostsAsync();
                    }
                }
                @Override
                public void onCancel() {}
                @Override
                public void onError(FacebookException error) {}
            });
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_pages"));
        } else {
            getPostsAsync();
        }
    }

    private void getPostsAsync() {
        //TODO: diff update
        facebookPostList = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putString("is_published", "false");
        bundle.putString("access_token", facebookPageAccessToken);
        GraphRequest getUnpublishedPostsRequest = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/promotable_posts", facebookPageId),
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject page = data.getJSONObject(i);
                                if (page.has("message")) {
                                    String id = page.getString("id");
                                    String message = page.getString("message");
                                    String createdTime = page.getString("created_time");
                                    facebookPostList.add(new FacebookPost(id, message, createdTime));
                                } else {
                                    //TODO: story, etc.
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        bundle = new Bundle();
        bundle.putString("access_token", facebookPageAccessToken);
        GraphRequest getPublishedPostsRequest = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/feed", facebookPageId),
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject page = data.getJSONObject(i);
                                if (page.has("message")) {
                                    String id = page.getString("id");
                                    String message = page.getString("message");
                                    String createdTime = page.getString("created_time");
                                    facebookPostList.add(new FacebookPost(id, message, createdTime));
                                } else {
                                    //TODO: story, etc.
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        GraphRequestBatch batch = new GraphRequestBatch(Arrays.asList(getUnpublishedPostsRequest, getPublishedPostsRequest));
        batch.addCallback(new GraphRequestBatch.Callback() {
            public void onBatchCompleted(GraphRequestBatch batch) {
                updateFacebookPostItems();
            }
        });
        batch.executeAsync();
    }

    private void updateFacebookPostItems() {
        //TODO: cache and only reload when explicitly commanded
        LinearLayout layout = (LinearLayout)findViewById(R.id.content_page_detail_linearlayout);
        layout.removeAllViewsInLayout();
        for (FacebookPost post : facebookPostList) {
            PostItemTile tile = new PostItemTile(this);
            tile.setContent(post);
            updatePostDetail(post, "is_published", tile); //TODO: How can I get scheduled publish time??
            updatePostInsight(post, "post_impressions_unique", (TextView)tile.findViewById(R.id.post_item_view_count));
            layout.addView(tile);
        }
    }

    private void updatePostDetail(final FacebookPost post,
                                  final String fieldName,
                                  final PostItemTile tile) {
        final TextView textView = (TextView)tile.findViewById(R.id.post_item_publish_status);
        final LinearLayout linearLayout = (LinearLayout)tile.findViewById(R.id.post_item_tile_linearlayout);
        Bundle bundle = new Bundle();
        bundle.putString("fields", fieldName);
        bundle.putString("access_token", facebookPageAccessToken);
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
                        textView.setText(post.getPublishStatus());
                        if (!post.isPublished())
                            // The following line requires API level 16 - current is 15
                            //linearLayout.setBackground(R.drawable.border_dark);
                            linearLayout.setBackgroundResource(R.drawable.border_dark);
                    }
                }
        ).executeAsync();
    }

    private void updatePostInsight(final FacebookPost post,
                                   final String insightName,
                                   final TextView view) {
        //TODO: permission check - "read_insights"
        Bundle bundle = new Bundle();
        bundle.putString("access_token", facebookPageAccessToken);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/insights/%s", post.getId(), insightName),
                bundle,
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
                        view.setText(String.valueOf(post.getViewCount()));
                    }
                }
        ).executeAsync();
    }
}
