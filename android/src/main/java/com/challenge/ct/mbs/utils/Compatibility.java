package com.challenge.ct.mbs.utils;

import android.app.Activity;
import android.app.Dialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Compatibility {
    public static final int COMPATIBILITY_REQUEST_CODE = 1001;

    private static GoogleApiAvailability instance = GoogleApiAvailability.getInstance();

    // unreliable with fcm messaging service
    public static boolean hasCompatibleGoogleApi(Activity activity) {
        int state = instance.isGooglePlayServicesAvailable(activity);

        if (state == ConnectionResult.SUCCESS) {
            return true;
        }

        Dialog dialog = instance.getErrorDialog(activity, state, COMPATIBILITY_REQUEST_CODE);
        if (dialog != null) {
            dialog.show();
        }

        return false;
    }
}
