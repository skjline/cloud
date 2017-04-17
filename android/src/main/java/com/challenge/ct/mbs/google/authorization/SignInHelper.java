package com.challenge.ct.mbs.google.authorization;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.utils.Const;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

public class SignInHelper {
    private GoogleApiClient googleClient;

    public SignInHelper() {

    }

    public boolean isInit() {
        return googleClient != null;
    }

    public void initializeGoogleApi(FragmentActivity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void requestGoogleSignIn(FragmentActivity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
        activity.startActivityForResult(signInIntent, Const.GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    public void requestSignOut() {
        requestSignOut(null);
    }

    public void requestSignOut(final Callback callback) {
        if (googleClient == null) {
            return;
        }

        if (callback != null) {
            callback.onUserSignedOut();
        }
    }

    public interface Callback {
        void onUserSignedOut();
    }
}
