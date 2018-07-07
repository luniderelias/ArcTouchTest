package com.arctouch.codechallenge.View.home;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.home_activity)
public class HomeActivity extends AppCompatActivity {

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @Bean
    MovieService movieService;

    MovieAdapter movieAdapter;
    public static final String[] permissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    List<Movie> movies = new ArrayList<>();
    private EndlessRecyclerViewScrollListener scrollListener;
    static Movie movie;

    @AfterViews
    void afterViews() {
        PermissionUtil.requestPermissions(this, permissions, 123);
        configureRecyclerView();
        getMovies(1L);
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

    @Background
    void getMovies(Long page) {
        movieService.getMoviesRest(page, "teste")
                .onErrorResumeNext(response -> {
                }).subscribe(response -> {
            saveMoviesToCache();
            movies = response.results;
            movieAdapter.addMovies(movies);
            progressBar.setVisibility(View.GONE);
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
                getMovies(page.longValue());
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
