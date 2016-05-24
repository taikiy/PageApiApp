package com.example.taikiy.pageapiapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
        if (!((PageApiApp) this.getApplication()).hasPublishPermission()) {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    //TODO: show dialog - publishing a post requires "publish_pages"
                }
                @Override
                public void onCancel() {}
                @Override
                public void onError(FacebookException error) {}
            });
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_pages"));
        }

        setContentView(R.layout.activity_create_post);

        facebookPageId = getIntent().getStringExtra("pageId");
        facebookPageName = getIntent().getStringExtra("pageName");
        facebookPageAccessToken = getIntent().getStringExtra("pageAccessToken");
        setTitle(String.format("Post as %s", facebookPageName));

        final PageApiApp app = (PageApiApp)this.getApplication();
        EditText editText = (EditText)findViewById(R.id.create_post_message);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = new String(s.toString());
                Button button = (Button)findViewById(R.id.create_post_publish_button);
                if (app.hasPublishPermission()) {
                    if (str.trim().length() > 0) {
                        button.setEnabled(true);
                    } else {
                        button.setEnabled(false);
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        final TextView scheduleDateTextView = (TextView)findViewById(R.id.create_post_publish_date_textview);
        final TextView scheduleTimeTextView = (TextView)findViewById(R.id.create_post_publish_time_textview);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar timestamp = Calendar.getInstance();
        timestamp.add(Calendar.HOUR, 1); // Scheduled publish time is between ten minutes to six months
        scheduleDateTextView.setText(dateFormat.format(timestamp.getTime()));
        scheduleTimeTextView.setText(timeFormat.format(timestamp.getTime()));

        Button postButton = (Button) findViewById(R.id.create_post_publish_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.create_post_message);
                RadioButton radioButton = (RadioButton)findViewById(R.id.create_post_immediate_radiobutton);
                //TODO: better to use RadioButtonGroup to get the selected radio button (not sure if that's possible though)
                if (radioButton.isChecked()) {
                    publishPost(editText.getText().toString());
                } else {
                    Calendar timestamp = Calendar.getInstance();
                    try {
                        SimpleDateFormat scheduleTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date scheduleTime = scheduleTimeFormat.
                                parse(scheduleDateTextView.getText().toString() + " " +
                                        scheduleTimeTextView.getText().toString());
                        timestamp.setTimeInMillis(scheduleTime.getTime());

                        Calendar minTime = Calendar.getInstance();
                        Calendar maxTime = Calendar.getInstance();
                        minTime.add(Calendar.MINUTE, 10);
                        minTime.add(Calendar.MONTH, 6);
                        if (timestamp.getTimeInMillis() < minTime.getTimeInMillis() + 5000 || // Add an extra 5 secs to be safe
                                timestamp.getTimeInMillis() > maxTime.getTimeInMillis()) {
                            //TODO: show error dialog

                        }
                    } catch (ParseException e) {
                        //TODO: error handle
                        e.printStackTrace();
                    }
                    publishPost(editText.getText().toString(), timestamp.getTime());
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        RadioButton button = (RadioButton)view;
        GridLayout layout = (GridLayout)findViewById(R.id.create_post_datetime_picker_layout);
        switch (button.getId()) {
            case R.id.create_post_immediate_radiobutton:
                if (button.isChecked())
                    layout.setVisibility(View.GONE);
                break;
            case R.id.create_post_schedule_radiobutton:
                if (button.isChecked())
                    layout.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onDateTextViewClicked(View view) {
        final TextView textView = (TextView)view;
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar calendar = Calendar.getInstance();
        try {
            Date date = dateFormat.parse(textView.getText().toString());
            calendar.setTime(date);
        } catch (ParseException e) {
            //TODO: error handle
        }
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                textView.setText(dateFormat.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public void onTimeTextViewClicked(View view) {
        final TextView textView = (TextView)view;
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        final Calendar calendar = Calendar.getInstance();
        try {
            Date time = timeFormat.parse(textView.getText().toString());
            calendar.setTime(time);
        } catch (ParseException e) {
            //TODO: error handle
        }
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(1970, 1, 1, hourOfDay, minute);
                        textView.setText(timeFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void publishPost(String message) {
        this.publishPost(message, null);
    }

    private void publishPost(String message, @Nullable Date scheduledPublishTime) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putString("access_token", facebookPageAccessToken);
        if (scheduledPublishTime != null) {
            bundle.putBoolean("published", false);
            bundle.putLong("scheduled_publish_time", scheduledPublishTime.getTime() / 1000);
        }
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/feed", facebookPageId),
                bundle,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //TODO: Error handle
                        EditText editText = (EditText)findViewById(R.id.create_post_message);
                        editText.setText(response.toString());

                        //TODO: show a result dialog
                        //TODO: go back to details page
                    }
                }
        ).executeAsync();
    }
}
