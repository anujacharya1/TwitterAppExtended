package com.anuj.twitter.activities;

import android.content.Intent;
import android.os.Parcel;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.anuj.twitter.DBHelper;
import com.anuj.twitter.R;
import com.anuj.twitter.TwitterApplication;
import com.anuj.twitter.TwitterClient;
import com.anuj.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.anuj.twitter.adapters.SampleFragmentPagerAdapter;
import com.anuj.twitter.adapters.TwitterTimelineAdapter;
import com.anuj.twitter.dao.TimelineDO;
import com.anuj.twitter.dao.UserDO;
import com.anuj.twitter.dialogs.ComposeTweetDialog;
import com.anuj.twitter.models.Timeline;
import com.anuj.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.Header;
import org.parceler.Parcels;

import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import android.text.Editable;
import android.text.TextWatcher;

public class TwitterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        //toolbar set
        setToolbar();
        setupTheFrangment();

    }

    private void setupTheFrangment(){
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                TwitterActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showComponseTweetDialog(){

        //show the dialog framgment
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        //set the dilaog framgment
        DialogFragment newFragment = ComposeTweetDialog.newInstance(new ComposeTweetDialog.ComposeTweetDialogListner() {
            @Override
            public void onTweet(String text) {

                // got the value from the dialog
                // put in the list view and refresh the adapter
                // populate the listview if the text is not empty
                // TODO: How to handle this now ?
                // on the compose tweet send this data to frangment
                // how to do this ?
//                if (text != null && !text.isEmpty()) {
//                    postTheUserTweetOnTheList(text);
//                }

            }
        });
        newFragment.show(ft, "TWEET_DIALOG");
    }

    private void setToolbar(){
        Toolbar twitterToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(twitterToolBar);
        twitterToolBar.setBackgroundColor(getResources().getColor(R.color.blue));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_twitter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compose:
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        showComponseTweetDialog();
                        return false;
                    }
                });
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
