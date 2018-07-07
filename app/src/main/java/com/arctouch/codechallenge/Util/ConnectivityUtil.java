package com.arctouch.codechallenge.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Objects;

public class ConnectivityUtil {

    public static boolean hasNetworkConnection(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) Objects.requireNonNull(context
                .getSystemService(Context.CONNECTIVITY_SERVICE)))
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}

