package com.anuj.twitter.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.widget.Toast;

import com.anuj.twitter.R;
import com.anuj.twitter.dialogs.ComposeTweetDialog;
import com.anuj.twitter.fragments.MentionFragment;
import com.anuj.twitter.fragments.PageFragment;
import com.anuj.twitter.fragments.ProfileFragment;
import com.anuj.twitter.fragments.SmartFragmentStatePagerAdapter;
import com.anuj.twitter.models.User;

public class TwitterActivity extends AppCompatActivity
        implements PageFragment.OnItemSelectedListener{


    private SmartFragmentStatePagerAdapter adapterViewPager;
    ViewPager viewPager;

    @Override
    public void onProfilePicClicked(User user) {
        Log.i("INFO","got the profile pic from the fragment user="+user);
        showProfile(user);
    }


    public static class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PageFragment.newInstance(0);
                case 1:
                    return MentionFragment.newInstance(0);

                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "@Home";
            } else if (position == 1) {
                return "@Mention";
            } else {
                return "Page " + position;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        //toolbar set
        setToolbar();
        setupTheFrangment();

        System.out.println("");
    }

    private void setupTheFrangment() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showComponseTweetDialog() {

        //show the dialog framgment
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        //set the dilaog framgment
        DialogFragment newFragment = ComposeTweetDialog.newInstance(new ComposeTweetDialog.ComposeTweetDialogListner() {
            @Override
            public void onTweet(String text) {
                if (text != null && !text.isEmpty()) {
                    Fragment f = adapterViewPager.getRegisteredFragment(0);
                    PageFragment pageFragment = (PageFragment) adapterViewPager.getRegisteredFragment(viewPager.getCurrentItem());

                    pageFragment.postTheUserTweetOnTheList(text);
                }
            }
        });
        newFragment.show(ft, "TWEET_DIALOG");
    }

    private void setToolbar() {
        Toolbar twitterToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(twitterToolBar);
        twitterToolBar.setBackgroundColor(getResources().getColor(R.color.blue));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * This is goign to be show the profile frangment both for the current user
     * and anyone clicking form the fragment
     *
     * @param user
     */
    private void showProfile(User user){

        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("user", user);
        startActivity(i);
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
                break;

            case R.id.action_profile:
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        showProfile(null);
                        return false;
                    }
                });
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}
