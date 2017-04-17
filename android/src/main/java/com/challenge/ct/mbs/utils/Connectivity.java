package com.challenge.ct.mbs.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Connectivity {
    /**
     * Check if wifi is connected
     */
    public static boolean isConnected(Context context) {
        // todo: checks wifi connection only, update  4G
        ConnectivityManager connection = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connection.getActiveNetworkInfo() == null ||
                !connection.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
