package com.youngseum.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Young on 2016-05-10.
 */
public class MovieDetail implements Parcelable{
    String title;
    String poster;
    String overview;
    String release;
    String vote;
    String popularity;

    public MovieDetail(String oTitle, String oPoster, String oOverview, String oRelease, String oVote, String oPopularity)
    {
        this.title = oTitle;
        this.poster = oPoster;
        this.overview = oOverview;
        this.release = oRelease;
        this.vote = oVote;
        this.popularity = oPopularity;
    }

    public MovieDetail(Parcel in) {
        title = in.readString();
        poster = in.readString();
        overview = in.readString();
        release = in.readString();
        vote = in.readString();
        popularity = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(poster);
        parcel.writeString(overview);
        parcel.writeString(release);
        parcel.writeString(vote);
        parcel.writeString(popularity);
    }

    public static final Parcelable.Creator<MovieDetail> CREATOR = new Parcelable.Creator<MovieDetail>() {
        @Override
        public MovieDetail createFromParcel(Parcel source) {
            return new MovieDetail(source);
        }

        @Override
        public MovieDetail[] newArray(int i) {
            return new MovieDetail[i];
        }
    };
}
