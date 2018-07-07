package com.arctouch.codechallenge.View.home;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arctouch.codechallenge.R;
import com.arctouch.codechallenge.Service.MovieService;
import com.arctouch.codechallenge.Util.EndlessRecyclerViewScrollListener;
import com.arctouch.codechallenge.Model.Cache;
import com.arctouch.codechallenge.Model.Genre;
import com.arctouch.codechallenge.Model.Movie;
import com.arctouch.codechallenge.Util.PermissionUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
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

    MovieAdapter movieAdapter;
    public static final String[] permissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    String query = "Lançamentos";

    List<Movie> movies = new ArrayList<>();
    private EndlessRecyclerViewScrollListener scrollListener;
    static Movie movie;
    Long currentPage = 1L;

    @AfterViews
    void afterViews() {
        PermissionUtil.requestPermissions(this, permissions, 123);
        onTextChange(searchEditText);
        configureRecyclerView();
        getMovies();
        setMoviesAdapter();
    }

    @UiThread
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

    @UiThread
    void onTextChange(EditText searchEditText) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                movieAdapter.setMovies(new ArrayList<>());
                if(!s.toString().equals(""))
                    query = s.toString();
                query = query.replace(" ","+");
                getMovies();
            }
        });
    }

    @Background
    void getMovies() {
        movieService.getMoviesRest(currentPage, query)
                .onErrorResumeNext(response -> {
                }).subscribe(response -> {
            saveMoviesToCache();
            movies = response.results;
            movieAdapter.addMovies(movies);
            movieAdapter.notifyDataSetChanged();
        }).isDisposed();

    }

    private void configureRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new GridLayoutManager(
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

    private void saveMoviesToCache() {
        for (Movie movie : movies) {
            movie.genres = new ArrayList<>();
            for (Genre genre : Cache.getGenres()) {
                if (movie.genreIds.contains(genre.id)) {
                    movie.genres.add(genre);
                }
            }
        }
    }
}
