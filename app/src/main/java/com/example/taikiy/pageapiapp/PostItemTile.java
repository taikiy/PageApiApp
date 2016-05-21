package com.example.taikiy.pageapiapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.taikiy.pageapiapp.model.FacebookPost;

import org.w3c.dom.Text;

/**
 * Created by taikiy on 2016/05/21.
 */
public class PostItemTile extends LinearLayout {
    private Context context;
    private TextView messageTextView;
    private TextView createdTimeTextView;
    private TextView viewCountTextView;
    private TextView publishStatusTextView;

    public PostItemTile(Context context) {
        super(context);
        this.context = context;
        initialize();
    }

    public PostItemTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initialize();
    }

    private void initialize() {
        View layout = LayoutInflater.from(context).inflate(R.layout.post_item_tile, this);
        messageTextView = (TextView)layout.findViewById(R.id.post_item_message);
        createdTimeTextView = (TextView)layout.findViewById(R.id.post_item_created_time);
        viewCountTextView = (TextView)layout.findViewById(R.id.post_item_view_count);
        publishStatusTextView = (TextView)layout.findViewById(R.id.post_item_publish_status);
    }

    public void setContent(FacebookPost post) {
        messageTextView.setText(post.getMessage());
        createdTimeTextView.setText(post.getCreatedTimeString());
        viewCountTextView.setText(String.format("Seen by %d", post.getViewCount()));
        publishStatusTextView.setText("");
    }
}
