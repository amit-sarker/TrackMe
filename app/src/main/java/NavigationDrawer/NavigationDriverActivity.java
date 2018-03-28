package NavigationDrawer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ChatBot.ChatBotActivity;
import Profile.EditProfileActivity;
import Profile.ProfileActivity;
import com.example.amit.trackmeNew.R;
import com.example.amit.trackmeNew.SelectionActivity;
import com.google.firebase.auth.FirebaseAuth;

import Feed.FeedActivity;
import HeatMap.HeatMapsActivity;

public class NavigationDriverActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private Button srch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_view);
        setupToolbarMenu();
        setupNavigationDrawerMenu();
        srch = findViewById(R.id.search_button);

    }

    private void setupToolbarMenu() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
    }

    private void setupNavigationDrawerMenu() {

        NavigationView navigationView = findViewById(R.id.navigationView);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override // Called when Any Navigation Item is Clicked
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        String itemName = (String) menuItem.getTitle();
        closeDrawer();
        switch (menuItem.getItemId()) {

            case R.id.signout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(NavigationDriverActivity.this, SelectionActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.feed:
                Intent intent1 = new Intent(NavigationDriverActivity.this, FeedActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.item_edit:
                Intent intent2 = new Intent(NavigationDriverActivity.this, EditProfileActivity.class);
                intent2.putExtra("user", "Driver");
                startActivity(intent2);
                break;
            case R.id.heatmap:
                Intent intent3 = new Intent(NavigationDriverActivity.this, HeatMapsActivity.class);
                startActivity(intent3);
                break;

            case R.id.bot:
                Intent intent4 = new Intent(NavigationDriverActivity.this, ChatBotActivity.class);
                startActivity(intent4);
                break;
            case R.id.item_profile:
                Intent intent5 = new Intent(NavigationDriverActivity.this,ProfileActivity.class);
                startActivity(intent5);
                break;
        }

        return true;
    }

    // Close the Drawer
    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    // Open the Drawer
    private void showDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            closeDrawer();
        else
            super.onBackPressed();
    }



    public void addContentView(int layoutId) {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(layoutId, null, false);
        mDrawerLayout.addView(contentView, 0);
    }
}

