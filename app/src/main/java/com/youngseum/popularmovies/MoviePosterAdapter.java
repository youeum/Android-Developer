package com.youngseum.popularmovies;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Young on 2016-11-18.
 */

public class MoviePosterAdapter extends ArrayAdapter<MovieDetail>  {
    private static final String LOG_TAG = MovieDetail.class.getSimpleName();


    /**
     * This is our own custom constructor.
     * The context is used to inflate the layout file, and the list is the data from which
     * we populate the grid.
     * @param context        The current context. Used to inflate the layout file.
     * @param movieDetails  A list of MovieDetail objects to display in a grid
     */
    public MoviePosterAdapter(Activity context, List<MovieDetail> movieDetails) {
        super(context, 0, movieDetails);
        Log.v(LOG_TAG, "Successfully constructed MoviePosterAdapter");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieDetail mMovieDetail = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.poster_image);
        Picasso.with(getContext()).load(mMovieDetail.poster).into(posterView);

        TextView titleView = (TextView) convertView.findViewById(R.id.poster_title);
        titleView.setText(mMovieDetail.title);

        return convertView;
    }
}
