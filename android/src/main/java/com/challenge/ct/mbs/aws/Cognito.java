package com.challenge.ct.mbs.aws;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.challenge.ct.mbs.object.Credential;
import com.challenge.ct.mbs.utils.Const;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * A amazon Cognito client interface
 * <p>
 * todo: For easier development unauthorized access is enabled. change update method when release
 */

public class Cognito {
    private static final String GOOGLE_SIGN_IN_PROVIDER = "accounts.google.com";
    private CognitoCredentialsProvider credentialsProvider;

    private String awsAccessToken;

    public Cognito() {
    }

    public boolean isAuthorized() {
        return !TextUtils.isEmpty(awsAccessToken);
    }

    public CognitoCredentialsProvider getProvider() {
        return credentialsProvider;
    }

    public Single<String> activate(Context context, Credential credential) {
        if (TextUtils.isEmpty(credential.getPoolId()) ||
                TextUtils.isEmpty(credential.getRegion())) {
            throw new IllegalStateException(Const.ERROR_INVALID_AWS_CRENDETIAL);
        }


        credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                credential.getPoolId(), Regions.fromName(credential.getRegion()));

        // it tends to cache old credential and doesn't appropriately clear them
        credentialsProvider.clearCredentials();

        return Single.defer(() ->
                Single.fromCallable(credentialsProvider.getCredentials()::getSessionToken))
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> {
                    Log.e("Cognito SignIn", throwable.getMessage());
                    return "";
                })
                .map(token -> awsAccessToken = token);
    }

    /**
     * Add google access token as an OpenID to gain access level to AWS instance
     *
     * @param token google OpenId token
     */
    public void addGoogleAccessToken(String token) {
        if (TextUtils.isEmpty(token)) {
            return;
        }

        Map<String, String> logins = credentialsProvider.getLogins();
        if (logins == null) {
            logins = new HashMap<>();
        } else if (logins.containsKey(GOOGLE_SIGN_IN_PROVIDER)) {
            if (logins.get(GOOGLE_SIGN_IN_PROVIDER).equals(token)) {
                return;
            }

            logins.remove(GOOGLE_SIGN_IN_PROVIDER);
        }

        logins.put(GOOGLE_SIGN_IN_PROVIDER, token);
        credentialsProvider.setLogins(logins);
    }
}
