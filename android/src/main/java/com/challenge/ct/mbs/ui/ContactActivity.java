package com.challenge.ct.mbs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.google.authorization.SignInHelper;
import com.challenge.ct.mbs.ui.fragment.AbstractFragment;
import com.challenge.ct.mbs.ui.fragment.SignInFragment;
import com.challenge.ct.mbs.utils.Compatibility;
import com.challenge.ct.mbs.utils.Const;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

public class ContactActivity extends AppCompatActivity
        implements AbstractFragment.FragmentEventListener, SignInHelper.Callback {
    private AbstractFragment fragment;
    private View baseView;

    private DialogFragment dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setAppTitle();

        baseView = findViewById(R.id.layout_base);

        // return here if saved instance exist
        if (savedInstanceState != null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Const.TRANSITION_FRAGMENT_ID, R.layout.fragment_contacts);

        makeFragmentTransition(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        Compatibility.hasCompatibleGoogleApi(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        closeSignInDialog();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fm = getSupportFragmentManager();

        fm.popBackStack();
        if (fm.getBackStackEntryCount() > 1) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuSignOut = menu.findItem(R.id.menu_action_sign_out);
        if (menuSignOut == null) {
            return true;
        }

        menuSignOut.setVisible(ContactApplication.isSignedIn());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_sign_out:
                ContactApplication.requestSignOut(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        fragment.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        switch (requestCode) {
            case Const.GOOGLE_SIGN_IN_REQUEST_CODE:
                handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onEventTriggered(AbstractFragment.FragmentEvent event) {
        switch (event.getType()) {
            case Const.REQUEST_FRAGMENT_INSERT:
                makeFragmentTransition((Bundle) event.getMessage());
                break;
            case Const.REQUEST_FRAGMENT_REMOVE:
                onBackPressed();
                break;
            case Const.GOOGLE_SIGN_IN_REQUEST_CODE:
                requestUserSignIn();
                break;
        }
    }

    @Override
    public void onUserSignedOut() {
        ContactApplication.setGoogleApiToken(null);
        invalidateOptionsMenu();
        setAppTitle();
    }

    private void requestUserSignIn() {
        dialog = new SignInFragment();
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void handleSignInResult(GoogleSignInResult result) {
        closeSignInDialog();

        // Signed in successfully, show authenticated UI.
        GoogleSignInAccount acct = result.getSignInAccount();

        if (!result.isSuccess() || (acct == null)) {
            setAppTitle();
            Snackbar.make(baseView, "Working as a Guest", Snackbar.LENGTH_LONG).show();
            return;
        }

        ContactApplication.setGoogleApiToken(acct.getIdToken());
        String name = TextUtils.isEmpty(acct.getGivenName()) ?
                TextUtils.isEmpty(acct.getFamilyName()) ? "no name" : acct.getFamilyName() :
                acct.getGivenName();

        Snackbar.make(baseView, "Welcome " + name, Snackbar.LENGTH_SHORT).show();
        setAppTitle(name);

        invalidateOptionsMenu();
    }

    private void setAppTitle() {
        setAppTitle(null);
    }

    private void setAppTitle(String user) {
        setTitle(getString(R.string.app_title) + " " + (TextUtils.isEmpty(user) ? "Guest" : user));
    }

    private void makeFragmentTransition(Bundle bundle) {
        if (bundle == null || !bundle.containsKey(Const.TRANSITION_FRAGMENT_ID)) {
            // fragment id not found
            return;
        }

        fragment = FragmentFactory.getFragment(bundle.getInt(Const.TRANSITION_FRAGMENT_ID));

        if (bundle.containsKey(Const.TRANSITION_CONTACT_UID)) {
            fragment.setArguments(bundle);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        invalidateOptionsMenu();
    }

    private void closeSignInDialog() {
        if (dialog == null) {
            return;
        }

        dialog.dismiss();
        dialog = null;
    }

}
