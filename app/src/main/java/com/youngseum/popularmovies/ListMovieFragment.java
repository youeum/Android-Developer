package com.youngseum.popularmovies;

import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListMovieFragment extends Fragment {

    private final String ListMovie_tag = ListMovieFragment.class.getSimpleName();

    public ListMovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        queryPopularMovies();
    }

    private void queryPopularMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, MovieDetail[]> {

        private final String FetchMovie_tag = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String that has movie data in JSON format
         * And Parse it.
         */
        private MovieDetail[] getMovieDataFromJson(String movieJasonStr)
            throws JSONException {

            // these are the names of the JSON objects that need to be extracted
            final int NUM_ATTR = 6;
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

            MovieDetail[] movieDetails = new MovieDetail[numMovies];

            for (int i = 0; i < numMovies; i++) {
                JSONObject movieObject = movieArray.getJSONObject(i);

                movieDetails[i] = new MovieDetail(
                        movieObject.getString(DB_TITLE),
                        movieObject.getString(DB_POSTER),
                        movieObject.getString(DB_OVERVIEW),
                        movieObject.getString(DB_RELEASE),
                        movieObject.getString(DB_VOTE),
                        movieObject.getString(DB_POPULARITY)
                );
            }

            return movieDetails;
        }

        @Override
        protected MovieDetail[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJasonStr = null;

            try {
                // Construct the URL for the movide database query
                String BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
                String API_PARAM = "api_key";

                if (params.length == 1 && params[0] == "Rating") {
                    BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
                }

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_PARAM, BuildConfig.MY_API_KEY)
                        .build();

                Log.v(FetchMovie_tag, "API: "+BuildConfig.MY_API_KEY);
                Log.v(FetchMovie_tag, "URI: "+builtUri);
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
                Log.e(FetchMovie_tag, e.getMessage(), e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(FetchMovie_tag, "Error closing stream", e);
                    }
                }
            }

            // if parsing succeeds, return the parsed data
            try {
                return getMovieDataFromJson(movieJasonStr);
            } catch (JSONException e) {
                Log.e(FetchMovie_tag, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieDetail[] movieDetails) {
            super.onPostExecute(movieDetails);
        }
    }
}
