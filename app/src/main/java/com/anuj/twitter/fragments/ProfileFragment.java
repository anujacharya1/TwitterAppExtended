package com.anuj.twitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuj.twitter.DBHelper;
import com.anuj.twitter.R;
import com.anuj.twitter.TwitterApplication;
import com.anuj.twitter.TwitterClient;
import com.anuj.twitter.TwitterUtils;
import com.anuj.twitter.activities.TweetDetailActivity;
import com.anuj.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.anuj.twitter.adapters.ProfileTimeLineAdapter;
import com.anuj.twitter.adapters.TwitterTimelineAdapter;
import com.anuj.twitter.dao.TimelineDO;
import com.anuj.twitter.models.Timeline;
import com.anuj.twitter.models.User;
import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by anujacharya on 2/27/16.
 */
public class ProfileFragment extends Fragment {

    public static final String PROFILE_PAGE = "PROFILE_PAGE";

    @Bind(R.id.ivDetailProfilePic)
    ImageView ivDetailProfilePic;
    @Bind(R.id.tvDetailUsername)
    TextView tvDetailUsername;
    @Bind(R.id.tvDetailScreenName)
    TextView tvDetailScreenName;
    @Bind(R.id.tvfollowers)
    TextView tvfollowers;
    @Bind(R.id.tvfollowing)
    TextView tvfollowing;

    TwitterClient twitterClient;

    @Bind(R.id.rvProfile)
    public RecyclerView timeLineRecycleView;

    LinearLayoutManager linearLayoutManager;

    List<Timeline> timelines;

    ProfileTimeLineAdapter profileTimeLineAdapter;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainer;

    private int sinceId = 0;
    User user;

    // based on the twitter doc send the first time null and subsequent the last one
    // https://dev.twitter.com/rest/public/timelines
    Long max_id = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        user = (User)getArguments().get("user");
        Log.i("INFO", "ProfileFragment user=" + user);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.layout_profile, container, false);
        ButterKnife.bind(this, view);

        twitterClient = TwitterApplication.getRestClient(); //singleton

        Log.i("INFO", "secret=" + twitterClient.checkAccessToken().getSecret());
        Log.i("INFO", "token=" + twitterClient.checkAccessToken().getToken());

        setupTheTimelineAdapter();

        if(user!=null){
            setUpTheUserInfo(user);
        }
        populateTimelineById(1, max_id);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.srlProfile);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //set the maxId to zero
                max_id = null;
                timelines.clear(); //clear list
                profileTimeLineAdapter.notifyDataSetChanged();
                populateTimelineById(1, max_id);
            }
        });
        return view;
    }

    private void setUpTheUserInfo(User user){
        //username
        tvDetailUsername.setText(Html.fromHtml("<b><font size='1' color='#000000'>"
                + user.getName() + "</font></b>"));
        tvDetailScreenName.setText(Html.fromHtml("<font size='0.2' color='#9E9E9E'>" +
                "@" + user.getScreenName() + "</font>"));

        String followersStr = user.getFollowers_count();
        String followingStr = user.getFriends_count();

        Long followers = Long.valueOf(followersStr);
        Long following = Long.valueOf(followingStr);

        if(followers>=1000){
            followersStr =  TwitterUtils.coolFormat(followers,0);
        }
        if(following >=1000){
            followingStr =  TwitterUtils.coolFormat(following,0);
        }

        tvfollowers.setText(user.getFollowers_count() == null ? "0" : followersStr + " Followers");
        tvfollowing.setText(user.getFriends_count() == null ? "0" : followingStr + " Following");

        //profile image
        Glide.with(getContext())
                .load(user.getProfileImg())
                .into(ivDetailProfilePic);
    }

    private void setupTheTimelineAdapter(){

        // set properties of recycler
        timeLineRecycleView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        timeLineRecycleView.setLayoutManager(linearLayoutManager);

        timelines = new LinkedList<>();
        profileTimeLineAdapter = new ProfileTimeLineAdapter(timelines);

        profileTimeLineAdapter.setOnItemClickListener(new ProfileTimeLineAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Timeline timeline = timelines.get(position);

                Intent i = new Intent(getActivity(), TweetDetailActivity.class);
                i.putExtra("timeline", Parcels.wrap(timeline));
                startActivity(i);
            }
        });

        //end less scroller
        timeLineRecycleView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int pageList, int totalItemsCount) {

                Log.i("INFO", "totalItemsCount=" + totalItemsCount);
                if (sinceId != pageList) {
                    Log.i("INFO", "sinceId=" + sinceId);
                    Log.i("INFO", "pageList=" + pageList);
                    populateTimelineById(++pageList, max_id);
                } else {
                    Log.e("ERROR", "sinceId=" + sinceId);
                    Log.e("ERROR", "pageList=" + pageList);
                }
            }
        });

        // give our custom adapter to the recycler view
        timeLineRecycleView.setAdapter(profileTimeLineAdapter);
    }

    private void populateTimelineById(int sinceId, final Long maxId){

        twitterClient.getUserTimeLineByUserId(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Timeline timeline = new Timeline();
                List<Timeline> timelineList = timeline.getFromJsonArray(response);

                if (timelineList != null && !timelineList.isEmpty()) {
                    Log.i("INFO", "timelines= " + timelineList.toString());
                    //if maxId is null i.e the first time we call add all the result to the list

                    if (maxId == null && user == null) {
                        // this is the case when we are not passing the user
                        // i.e when the user us clicking it;s own profile button
                        // TODO: what if the user does not have any tweet posted ? well I don't know
                        // I am doing all this to just get the user screenName and firstName etc.
                        Timeline timeline1 = timelineList.get(0);
                        setUpTheUserInfo(timeline1.getUser());


                    }
                    if (maxId == null) {
                        max_id = timelineList.get(timelineList.size() - 1).getId();
                        Log.i("INFO", "First Time going to set the max_id=" + max_id);
                        timelines.addAll(timelineList);
                        Log.i("INFO", "Got from twitter API count = " + timelines.size());

                    } else {

                        Log.i("INFO", "Subsequent ime going to set the max_id=" + max_id);

                        // remove the first element as it's going to be duplicate based on
                        // the twitter doc
                        timelineList.remove(0);
                        max_id = timelineList.get(timelineList.size() - 1).getId();
                        timelines.addAll(timelineList);
                    }

                    List<TimelineDO> timelineDOs = DBHelper.getAllInDescOfDate();
                    Log.i("INFO", "Stored in database count = " + timelineDOs.size());

                    int curSize = profileTimeLineAdapter.getItemCount();
                    profileTimeLineAdapter.notifyItemRangeInserted(curSize, timelines.size() - 1);

                    swipeContainer.setRefreshing(false);
                } else {
                    Log.e("ERROR", "Did not got data from twitter API");
                }

                Log.i("INFO", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                throwable.printStackTrace();
                Log.e("ERROR", "GOT THE ERROR FROM THE TWITTER");
            }
        }, sinceId, max_id, user != null ? String.valueOf(user.getId()) : null);
    }

    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragmentDemo = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragmentDemo.setArguments(args);
        return fragmentDemo;
    }
}
