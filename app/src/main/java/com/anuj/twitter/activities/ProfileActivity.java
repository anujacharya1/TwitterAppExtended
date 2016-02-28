package com.anuj.twitter.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.anuj.twitter.R;
import com.anuj.twitter.fragments.ProfileFragment;
import com.anuj.twitter.models.User;

/**
 * Created by anujacharya on 2/27/16.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        User user = (User)getIntent().getSerializableExtra("user");

        Log.i("INFO", "showProfile");
        // Let's first dynamically add a fragment into a frame container
        getSupportFragmentManager().beginTransaction().
                replace(R.id.profile_placeholder, ProfileFragment.newInstance(user), "PROFILE_PAGE").
                commit();
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
