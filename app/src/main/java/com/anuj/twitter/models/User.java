package com.anuj.twitter.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * Created by anujacharya on 2/15/16.
 */
@Parcel
public class User implements Serializable{

    Long id;

    String name;

    @SerializedName("profile_image_url")
    String profileImg;

    @SerializedName("screen_name")
    private String screenName;

    @SerializedName("friends_count")
    private String friends_count;

    @SerializedName("followers_count")
    private String followers_count;

    public String getFriends_count() {
        return friends_count;
    }

    public void setFriends_count(String friends_count) {
        this.friends_count = friends_count;
    }

    public String getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(String followers_count) {
        this.followers_count = followers_count;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", profileImg='" + profileImg + '\'' +
                ", screenName='" + screenName + '\'' +
                ", friends_count='" + friends_count + '\'' +
                ", followers_count='" + followers_count + '\'' +
                '}';
    }
}
