package com.arctouch.codechallenge.View.home;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.arctouch.codechallenge.R;
import com.arctouch.codechallenge.Service.MovieService;
import com.arctouch.codechallenge.Util.ConnectivityUtil;
import com.arctouch.codechallenge.Util.EndlessRecyclerViewScrollListener;
import com.arctouch.codechallenge.Model.Movie;
import com.arctouch.codechallenge.Util.PermissionUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.home_activity)
public class HomeActivity extends AppCompatActivity {

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @ViewById(R.id.searchEditText)
    EditText searchEditText;

    @Bean
    MovieService movieService;

    private EndlessRecyclerViewScrollListener scrollListener;

    List<Movie> movies = new ArrayList<>();
    static Movie movie;
    MovieAdapter movieAdapter;
    public final static String LIST_STATE_KEY = "recycler_list_state";
    Parcelable listState;
    LinearLayoutManager layoutManager;

    Long currentPage = 1L;
    private static final String INITIAL_QUERY = "A";
    String query = INITIAL_QUERY;

    public static final String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @AfterViews
    void afterViews() {
        PermissionUtil.requestPermissions(this, permissions, 123);
        fillQuery();
        configureRecyclerView();
        onTextChange(searchEditText);
        setMoviesAdapter();
        getMovies();
    }

    private void fillQuery() {
        if (!searchEditText.getText().toString().equals("")) {
            query = searchEditText.getText().toString();
            query = query.replace(" ", "+");
        } else {
            query = INITIAL_QUERY;
        }
    }

    private void configureRecyclerView() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(
                getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(Integer page, int totalItemsCount, RecyclerView view) {
                currentPage = page.longValue();
                getMovies();
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @UiThread
    void onTextChange(EditText searchEditText) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                movieAdapter.setMovies(new ArrayList<>());
                fillQuery();
                getMovies();
            }
        });
    }

    void setMoviesAdapter() {
        movieAdapter = new MovieAdapter(movies,
                item -> {
                    movie = item;
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_container, new MovieFragment_(), "MovieFragment")
                            .addToBackStack("MovieFragment")
                            .commit();
                });
        recyclerView.setAdapter(movieAdapter);
    }

    @Background
    void getMovies() {
        checkConnectivity();
        movieService.getMovies(currentPage, query)
                .onErrorResumeNext(response -> {
                }).subscribe(response -> updateMoviesList(response.results)).isDisposed();
    }

    private void updateMoviesList(List<Movie> movies) {
        this.movies = movies;
        movieAdapter.addMovies(movies);
        movieAdapter.notifyDataSetChanged();
    }

    @UiThread
    void checkConnectivity() {
        if (!ConnectivityUtil.hasNetworkConnection(this))
            Snackbar.make(recyclerView,
                    getString(R.string.not_connected),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.connect, view -> startActivityForResult(new Intent(
                            android.provider.Settings.ACTION_SETTINGS), 0))
                    .setActionTextColor(getResources()
                            .getColor(R.color.light_blue)).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        listState = recyclerView.getLayoutManager().onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
            query = searchEditText.getText().toString();
            getMovies();
            listState = state.getParcelable(LIST_STATE_KEY);
        }
    }
}
