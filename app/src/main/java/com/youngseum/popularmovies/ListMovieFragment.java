package com.youngseum.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class ListMovieFragment extends Fragment {

    private MoviePosterAdapter mMovieAdapter;
    private ArrayList<MovieDetail> movieList;

    // Use this to keep onItemSelected from firing when the view is being created
    private int lastSpinnerPos;

    public ListMovieFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        outState.putInt("lastSpinnerPos", lastSpinnerPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movieList = new ArrayList<>();
            lastSpinnerPos = 0;
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
            lastSpinnerPos = savedInstanceState.getInt("lastSpinnerPos");
        }

        // The arrayAdapter will take data from the database and
        // use it to populate the GridView it's attached to.
        mMovieAdapter = new MoviePosterAdapter(getActivity(), movieList);

        // If no saved data, query the database using popularity as the sort order
        if (savedInstanceState == null)
            queryPopularMovies("Popularlity");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieDetail detail = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_INTENT, detail);
                startActivity(intent);
            }
        });

        // Add a listener to the spinner and sort according to the setting chosen
        Spinner spinner = (Spinner) rootView.findViewById(R.id.sortBySpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // If the user did not click a different item, don't query the database
                if (lastSpinnerPos == i)
                    return;

                lastSpinnerPos = i;

                String selected = adapterView.getItemAtPosition(i).toString();
                if (selected.equals("Popularity")) {
                    queryPopularMovies("Popularity");
                }
                else if (selected.equals("Rating")) {
                    queryPopularMovies("Rating");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        return rootView;
    }

    private void queryPopularMovies(String setting) {
        // Check for the network status
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        // If no network available, don't query
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
        {
            Toast.makeText(getContext(), "No Network Connection", Toast.LENGTH_LONG).show();
            return;
        }

        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute(setting);
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

                movieDetails[i] =
                        new MovieDetail(
                            movieObject.getString(DB_TITLE),
                            movieObject.getString(DB_POSTER),
                            movieObject.getString(DB_OVERVIEW),
                            movieObject.getString(DB_RELEASE),
                            movieObject.getString(DB_VOTE),
                            movieObject.getString(DB_POPULARITY));
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
                // In default, it will query the movies according to the popularity
                String BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
                String API_PARAM = "api_key";

                if (params.length == 1 && params[0] == "Rating") {
                    BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
                }

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
            if (movieDetails != null) {
                mMovieAdapter.clear();
                for (MovieDetail detail : movieDetails) {
                    mMovieAdapter.add(detail);
                }
            }
        }
    }
}
