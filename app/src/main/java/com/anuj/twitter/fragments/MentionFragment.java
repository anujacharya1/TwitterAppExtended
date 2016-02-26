package com.anuj.twitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anuj.twitter.DBHelper;
import com.anuj.twitter.R;
import com.anuj.twitter.TwitterApplication;
import com.anuj.twitter.TwitterClient;
import com.anuj.twitter.activities.TweetDetailActivity;
import com.anuj.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.anuj.twitter.adapters.TwitterTimelineAdapter;
import com.anuj.twitter.dao.TimelineDO;
import com.anuj.twitter.models.Timeline;
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
 * Created by anujacharya on 2/25/16.
 */
public class MentionFragment extends Fragment {

    private static final String MENTION_PAGE = "MENTION_PAGE";

    private int mPage;

    TwitterClient twitterClient;

    @Bind(R.id.rvTimeline)
    public RecyclerView timeLineRecycleView;

    LinearLayoutManager linearLayoutManager;

    List<Timeline> timelines;

    TwitterTimelineAdapter twitterTimelineAdapter;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainer;

    private int sinceId = 0;

    // based on the twitter doc send the first time null and subsequent the last one
    // https://dev.twitter.com/rest/public/timelines
    Long max_id = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(MENTION_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        //set the singleton client
        twitterClient = TwitterApplication.getRestClient(); //singleton

        Log.i("INFO","secret="+ twitterClient.checkAccessToken().getSecret());
        Log.i("INFO", "token=" + twitterClient.checkAccessToken().getToken());

        setupTheTimelineAdapter();


        //Step1: try to get the data from database

        //attach the adapter to the listView
        //call the api and populate the timline
        //remember the first time is maxID is null
        populateMentionTimeline(1, max_id);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //set the maxId to zero
                max_id = null;
                timelines.clear(); //clear list
                twitterTimelineAdapter.notifyDataSetChanged();
                populateMentionTimeline(1, max_id);
            }
        });

        return view;
    }


    private void setupTheTimelineAdapter(){

        // set properties of recycler
        timeLineRecycleView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        timeLineRecycleView.setLayoutManager(linearLayoutManager);

        timelines = new LinkedList<>();
        twitterTimelineAdapter = new TwitterTimelineAdapter(timelines);

        twitterTimelineAdapter.setOnItemClickListener(new TwitterTimelineAdapter.OnItemClickListener() {
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
                    populateMentionTimeline(++pageList, max_id);
                } else {
                    Log.e("ERROR", "sinceId=" + sinceId);
                    Log.e("ERROR", "pageList=" + pageList);
                }
            }
        });

        // give our custom adapter to the recycler view
        timeLineRecycleView.setAdapter(twitterTimelineAdapter);
    }

    private void populateMentionTimeline(int sinceId, final Long maxId){

        twitterClient.getMentionTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Timeline timeline = new Timeline();
                List<Timeline> timelineList = timeline.getFromJsonArray(response);
                if (timelineList != null && !timelineList.isEmpty()) {
                    Log.i("INFO", "timelines= " + timelineList.toString());
                    //if maxId is null i.e the first time we call add all the result to the list
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

//                    List<TimelineDO> timelineDOs = DBHelper.getAllInDescOfDate();
//                    Log.i("INFO", "Stored in database count = " + timelineDOs.size());

                    int curSize = twitterTimelineAdapter.getItemCount();
                    twitterTimelineAdapter.notifyItemRangeInserted(curSize, timelines.size() - 1);

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
        }, sinceId, max_id);
    }



    public static MentionFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(MENTION_PAGE, page);
        MentionFragment fragment = new MentionFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
