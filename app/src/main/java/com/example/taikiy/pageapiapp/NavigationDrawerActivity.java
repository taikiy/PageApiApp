package com.example.taikiy.pageapiapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.SubMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taikiy.pageapiapp.model.Action;
import com.example.taikiy.pageapiapp.model.FacebookPage;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //private static final String PREFERENCES_FILE_NAME = "PreferencesFile";
    //private AccessToken accessToken;
    //private CallbackManager callbackManager;
    private List<FacebookPage> facebookPageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_drawer);

        updateNavigationMenuAsync();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_facebook_logout) {
            LoginManager.getInstance().logOut();
            showConfirmationDialog("Logout", "Successfully logged out from Facebook.", new Action() {
                @Override
                public void invoke() {
                    startActivity(new Intent(NavigationDrawerActivity.this, FacebookLoginActivity.class));
                }
            });
        } else if (id == R.id.nav_facebook_revoke_permissions) {
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/permissions",
                    null,
                    HttpMethod.DELETE,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            showConfirmationDialog("Revoke Permissions", response.toString(), new Action() {
                                @Override
                                public void invoke() {
                                    LoginManager.getInstance().logOut();
                                    startActivity(new Intent(NavigationDrawerActivity.this, FacebookLoginActivity.class));
                                }
                            });
                        }
                    }
            ).executeAsync();
        } else {
            FacebookPage page = null;
            for (int i = 0; i < facebookPageList.size(); i++) {
                if (facebookPageList.get(i).getMenuItemId() == id) {
                    page = facebookPageList.get(i);
                }
            }
            if (page != null) {
                Intent intent = new Intent(NavigationDrawerActivity.this, PageDetailActivity.class);
                intent.putExtra("pageId", page.getId());
                intent.putExtra("pageName", page.getName());
                intent.putExtra("pageAccessToken", page.getAccessToken());
                startActivity(intent);
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showConfirmationDialog(String title, String message, final Action action) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (action != null)
                            action.invoke();
                    }
                });
        //alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateNavigationMenuAsync() {
        //TODO: check permission - "pages_show_list"
        facebookPageList = new ArrayList<>();
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/accounts",
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
                                String name = page.getString("name");
                                String category = page.getString("category");
                                String accessToken = page.getString("access_token");
                                facebookPageList.add(new FacebookPage(id, name, category, accessToken));
                            }
                        } catch (JSONException e) {
                            //TODO: error handle
                            e.printStackTrace();
                        }
                        updateFacebookPageNavigationMenuItems();
                    }
                }
        ).executeAsync();
    }

    private void updateFacebookPageNavigationMenuItems() {
        //TODO: cache and only reload when explicitly commanded
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        SubMenu subMenu = navigationView.getMenu().findItem(R.id.nav_facebook_page_list).getSubMenu();
        if (subMenu.getItem(0).getItemId() == R.id.nav_facebook_page_placeholder) {
            subMenu.clear();
            for (int i = 0; i < facebookPageList.size(); i++) {
                FacebookPage page = facebookPageList.get(i);
                page.setMenuItemId(i);
                subMenu.add(Menu.NONE, i, i, page.getName());
                //TODO: Add icon
            }
        }
    }
}
