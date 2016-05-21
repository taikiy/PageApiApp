package com.example.taikiy.pageapiapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class CreatePostActivity extends AppCompatActivity {
    //TODO: implement serializable in FacebookPage and pass the instance
    private String facebookPageId;
    private String facebookPageName;
    private String facebookPageAccessToken;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Permission check
        if (!((PageApiApp)this.getApplication()).hasPublishPermission()) {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    //TODO: permission check - publishing a post requires "publish_pages"
                }

                @Override
                public void onCancel() {}

                @Override
                public void onError(FacebookException error) { }
            });
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_pages"));
        }

        setContentView(R.layout.activity_create_post);

        facebookPageId = getIntent().getStringExtra("pageId");
        facebookPageName = getIntent().getStringExtra("pageName");
        facebookPageAccessToken = getIntent().getStringExtra("pageAccessToken");
        setTitle(String.format("Post as %s", facebookPageName));

        Button postButton = (Button)findViewById(R.id.create_post_publish_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.create_post_message);
                publishPost(editText.getText().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void publishPost(String message) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putString("access_token", facebookPageAccessToken);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/feed", facebookPageId),
                bundle,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /*
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray data = json.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {

                                JSONObject page = data.getJSONObject(i);
                                String id = page.getString("id");
                                String name = page.getString("name");
                                String category = page.getString("category");
                                String accessToken = page.getString("access_token");
                                facebookPageList.add(new FacebookPage(id, name, category, accessToken));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        */
                        EditText editText = (EditText)findViewById(R.id.create_post_message);
                        editText.setText(response.toString());
                    }
                }
        ).executeAsync();
    }
}
