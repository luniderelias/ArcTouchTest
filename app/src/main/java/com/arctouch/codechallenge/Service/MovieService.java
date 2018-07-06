package com.arctouch.codechallenge.Service;

import com.arctouch.codechallenge.Model.UpcomingMoviesResponse;
import com.arctouch.codechallenge.Rest.TmdbApi;
import com.arctouch.codechallenge.Util.RestUtil;

import org.androidannotations.annotations.EBean;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class MovieService {

    public synchronized Observable<UpcomingMoviesResponse> getMoviesRest()  {
        return RestUtil.api.upcomingMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, 1L, TmdbApi.DEFAULT_REGION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
