package com.arctouch.codechallenge.View.home;

import android.app.Fragment;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.arctouch.codechallenge.Model.Cache;
import com.arctouch.codechallenge.Model.Genre;
import com.arctouch.codechallenge.Model.Movie;
import com.arctouch.codechallenge.R;
import com.arctouch.codechallenge.Util.MovieImageUrlBuilder;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EFragment(R.layout.fragment_movie)
public class MovieFragment extends Fragment {

    @ViewById(R.id.posterImageView)
    ImageView posterImageView;

    @ViewById(R.id.backdropImageView)
    ImageView backdropImageView;

    @ViewById(R.id.titleTextView)
    TextView titleTextView;

    @ViewById(R.id.descriptionTextView)
    TextView descriptionTextView;

    @ViewById(R.id.genresTextView)
    TextView genresTextView;

    @ViewById(R.id.releaseDateTextView)
    TextView releaseDateTextView;

    Movie movie;
    private final MovieImageUrlBuilder movieImageUrlBuilder = new MovieImageUrlBuilder();

    @AfterViews
    void afterViews() {
        movie = ((HomeActivity_) getActivity()).movie;
        loadImages();
        setTexts();
    }

    private void loadImages() {
        Picasso.get()
                .load(movieImageUrlBuilder.buildPosterUrl(movie.posterPath))
                .into(posterImageView);
        Picasso.get()
                .load(movieImageUrlBuilder.buildBackdropUrl(movie.backdropPath))
                .into(backdropImageView);
    }

    private void setTexts() {
        titleTextView.setText(movie.title);
        genresTextView.setText(getGendersText());
        releaseDateTextView.setText(movie.releaseDate.split("-")[0]);
        descriptionTextView.setText(movie.overview);
    }

    private StringBuilder getGendersText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Genre genre : Cache.getGenres()) {
            if (movie.genreIds.contains(genre.id)) {
                stringBuilder.append(genre.name + "  ");
            }
        }
        return stringBuilder;
    }
}
