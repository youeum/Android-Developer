package com.youngseum.popularmovies;

/**
 * Created by Young on 2016-05-10.
 */
public class MovieDetail {
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
}
