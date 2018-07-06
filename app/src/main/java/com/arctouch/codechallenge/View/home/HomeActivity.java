package com.arctouch.codechallenge.View.home;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arctouch.codechallenge.R;
import com.arctouch.codechallenge.Rest.TmdbApi;
import com.arctouch.codechallenge.Service.MovieService;
import com.arctouch.codechallenge.Util.PermissionUtil;
import com.arctouch.codechallenge.Util.RestUtil;
import com.arctouch.codechallenge.Model.Cache;
import com.arctouch.codechallenge.Model.Genre;
import com.arctouch.codechallenge.Model.Movie;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EActivity(R.layout.home_activity)
public class HomeActivity extends AppCompatActivity {

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @Bean
    MovieService movieService;

    public static final String[] permissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    @AfterViews
    void afterViews() {
        configureRecyclerView();
        PermissionUtil.requestPermissions(this, permissions, 123);

        movieService.getMoviesRest()
                .subscribe(response -> {
                    for (Movie movie : response.results) {
                        movie.genres = new ArrayList<>();
                        for (Genre genre : Cache.getGenres()) {
                            if (movie.genreIds.contains(genre.id)) {
                                movie.genres.add(genre);
                            }
                        }
                    }

                    recyclerView.setAdapter(new MovieAdapter(response.results));
                    progressBar.setVisibility(View.GONE);
                }).isDisposed();
    }

    private void configureRecyclerView() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
    }


}
