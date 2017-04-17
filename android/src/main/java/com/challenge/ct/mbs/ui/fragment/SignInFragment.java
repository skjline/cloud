package com.challenge.ct.mbs.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.R;
import com.google.android.gms.common.SignInButton;

public class SignInFragment extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        SignInButton btnGoogle = (SignInButton) view.findViewById(R.id.btn_google_sign_in);
        btnGoogle.setTag(R.id.btn_google_sign_in);
        btnGoogle.setOnClickListener(listener);

        Button btnGuest = (Button) view.findViewById(R.id.btn_guest_user);
        btnGuest.setTag(R.id.btn_guest_user);
        btnGuest.setOnClickListener(listener);

        getDialog().setTitle("Sign-In");
        return view;
    }

    private View.OnClickListener listener = view -> {
        if (view.getTag() == null) {
            return;
        }

        switch ((int) view.getTag()) {
            case R.id.btn_google_sign_in:
                ContactApplication.requestSignIn(getActivity());
                break;
            case R.id.btn_guest_user:
                break;
        }
        getDialog().dismiss();
    };

}
