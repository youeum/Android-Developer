package com.youngseum.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class ListMovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<MovieDetail>> {

    private MoviePosterAdapter mMovieAdapter;
    private ArrayList<MovieDetail> movieList;

    // Use this to keep onItemSelected from firing when the view is being created
    private int lastSpinnerPos;
    private String setting;

    @BindView(R.id.movie_grid)
    GridView gridView;

    @BindView(R.id.sortBySpinner)
    Spinner spinner;

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
            setting = "Popularity";
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
            lastSpinnerPos = savedInstanceState.getInt("lastSpinnerPos");
            setting = savedInstanceState.getString("setting");
        }

        // The arrayAdapter will take data from the database and
        // use it to populate the GridView it's attached to.
        mMovieAdapter = new MoviePosterAdapter(getActivity(), movieList);

        // If no saved data, query the database using popularity as the sort order
        if (savedInstanceState == null) {
            queryPopularMovies();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        // Get a reference to the GridView, and attach this adapter to it.
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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // If the user did not click a different item, don't query the database
                if (lastSpinnerPos == i)
                    return;

                lastSpinnerPos = i;

                setting = adapterView.getItemAtPosition(i).toString();
                queryPopularMovies();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return rootView;
    }

    @Override
    public void onLoadFinished(Loader<List<MovieDetail>> loader, List<MovieDetail> data) {
        mMovieAdapter.clear();
        mMovieAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<MovieDetail>> loader) {
        mMovieAdapter.clear();
    }

    @Override
    public Loader<List<MovieDetail>> onCreateLoader(int id, Bundle args) {
        return new MovieLoader(getActivity(), setting);
    }

    private void queryPopularMovies() {
        // Check for the network status
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        // If no network available, don't query
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
        {
            Toast.makeText(getContext(), "No Network Connection", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle b = new Bundle();
        b.putString("Setting", setting);
        getLoaderManager().restartLoader(0, b, this);


    }

}
