package com.challenge.ct.mbs;

import android.app.Application;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.challenge.ct.mbs.aws.Cognito;
import com.challenge.ct.mbs.aws.ContactDatabase;
import com.challenge.ct.mbs.google.authorization.SignInHelper;
import com.challenge.ct.mbs.object.Credential;

import io.reactivex.schedulers.Schedulers;

public class ContactApplication extends Application {
    private static ContactApplication application;

    public static ContactDatabase getDatabase() {
        return application.database;
    }

    public static void setGoogleApiToken(String account) {
        if (!TextUtils.isEmpty(application.googleApiToken) &&
                application.googleApiToken.equals(account)) {
            return;
        }

        application.googleApiToken = account;
        application.cognito.addGoogleAccessToken(account);
    }

    public static boolean isSignedIn() {
        return application.signHelper.isInit() && !TextUtils.isEmpty(application.googleApiToken);
    }

    public static void requestSignIn(FragmentActivity activity) {
        application.initializeSingInProvider(activity);
    }

    public static void requestSignOut(SignInHelper.Callback activity) {
        if (application.signHelper == null) {
            return;
        }

        application.signHelper.requestSignOut(activity);
    }

    public static Credential getCredential() {
        return application.credential;
    }

    public static Cognito getCognito() {
        return application.cognito;
    }

    // AWS Cloud DB interface instance
    private ContactDatabase database = new ContactDatabase();

    // signing method provider
    private SignInHelper signHelper = new SignInHelper();
    private String googleApiToken;

    private Credential credential;
    private Cognito cognito;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        initializeService();
    }

    private void initializeService() {
        application.credential = Credential.build(this);
        application.cognito = new Cognito();
        cognito.activate(this, credential)
                .observeOn(Schedulers.io())
                .subscribe(token -> application.database.initializeDatabase(cognito));
    }

    public void initializeSingInProvider(FragmentActivity activity) {
        if (!signHelper.isInit()) {
            signHelper.initializeGoogleApi(activity);
        }

        signHelper.requestGoogleSignIn(activity);
    }
}
