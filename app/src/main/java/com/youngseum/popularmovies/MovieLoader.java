package com.youngseum.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmatkr on 12/7/16.
 */

public class MovieLoader extends AsyncTaskLoader<List<MovieDetail>> {

    private enum Setting {
        POPULARITY ("Popularity"),
        RATING ("Rating");

        private final String name;

        Setting(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    private final String MovieLoader_tag = MovieLoader.class.getSimpleName();
    private Setting mSetting;
    private ArrayList<MovieDetail> mList;

    public MovieLoader(Context context, String setting) {
        super(context);

        if (setting.equals("Rating"))
            mSetting = Setting.RATING;
        else
            mSetting = Setting.POPULARITY;

    }

    @Override
    protected void onStartLoading() {
        if (mList != null) {
            deliverResult(mList);
        }

        if (mList == null)
            forceLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mList != null)
            mList = null;
    }

    private void getMovieDataFromJson(String movieJasonStr)
            throws JSONException {

        // these are the names of the JSON objects that need to be extracted
        final String DB_RESULT = "results";
        final String DB_TITLE = "title";
        final String DB_POSTER = "poster_path";
        final String DB_OVERVIEW = "overview";
        final String DB_RELEASE = "release_date";
        final String DB_VOTE = "vote_average";
        final String DB_POPULARITY = "popularity";

        JSONObject movieJson = new JSONObject(movieJasonStr);
        JSONArray movieArray = movieJson.getJSONArray(DB_RESULT);
        int numMovies = movieArray.length();

        if (mList == null)
            mList = new ArrayList<>();

        for (int i = 0; i < numMovies; i++) {
            JSONObject movieObject = movieArray.getJSONObject(i);

            mList.add(
                    new MovieDetail(
                            movieObject.getString(DB_TITLE),
                            movieObject.getString(DB_POSTER),
                            movieObject.getString(DB_OVERVIEW),
                            movieObject.getString(DB_RELEASE),
                            movieObject.getString(DB_VOTE),
                            movieObject.getString(DB_POPULARITY)
                    )
            );
        }

    }

    @Override
    public List<MovieDetail> loadInBackground() {
        Log.v(MovieLoader_tag, "Loading with the setting of: " + mSetting);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJasonStr = null;
        String BASE_URL;
        String API_PARAM = "api_key";

        try {
            // Construct the URL for the movie database query according to the setting
            if (mSetting == Setting.POPULARITY)
                BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
            else
                BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_PARAM, BuildConfig.MY_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the connection.
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            movieJasonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(MovieLoader_tag, e.getMessage(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(MovieLoader_tag, "Error closing stream", e);
                }
            }
        }

        // if parsing succeeds, return the parsed data
        try {
            getMovieDataFromJson(movieJasonStr);
            return mList;
        } catch (JSONException e) {
            Log.e(MovieLoader_tag, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }



}
