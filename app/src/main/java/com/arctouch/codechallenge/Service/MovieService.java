package com.arctouch.codechallenge.Service;

import com.arctouch.codechallenge.Model.MoviesResponse;
import com.arctouch.codechallenge.Model.UpcomingMoviesResponse;
import com.arctouch.codechallenge.Rest.TmdbApi;
import com.arctouch.codechallenge.Util.RestUtil;

import org.androidannotations.annotations.EBean;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class MovieService {

    public synchronized Observable<MoviesResponse> getMoviesRest(Long page, String query)  {
        return RestUtil.api.searchMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, page, TmdbApi.DEFAULT_REGION,query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
