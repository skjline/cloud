package com.challenge.ct.mbs.ui.component;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.object.User;

/**
 * File created on 3/6/17.
 */

public class UserEntry extends LinearLayout {
    private User user;

    private EditText etFirstName, etLastName, etPhoneNumber, etEMailAddress;

    public UserEntry(Context context) {
        super(context);
        initializeView(context);
    }

    public UserEntry(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public UserEntry(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    public UserEntry(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context);
    }

    private void initializeView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_user, this, true);

        etFirstName = (EditText) view.findViewById(R.id.et_first_name);
        etLastName = (EditText) view.findViewById(R.id.et_last_name);
        etPhoneNumber = (EditText) view.findViewById(R.id.et_phone_number);
        etEMailAddress = (EditText) view.findViewById(R.id.et_email);
    }

    @Override
    public void setEnabled(boolean bool) {
        super.setEnabled(bool);

        etFirstName.setEnabled(bool);
        etLastName.setEnabled(bool);
        etPhoneNumber.setEnabled(bool);
        etEMailAddress.setEnabled(bool);
    }

    public void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etPhoneNumber.setText("");
        etEMailAddress.setText("");
    }

    public User getUser() {
        if (!isInputValid()) {
            return null;
        }

        if (user == null) {
            // create a new user from populated fields
            return new User.Builder()
                    .setFirstName(etFirstName.getText().toString())
                    .setLastName(etLastName.getText().toString())
                    .setEmail(etEMailAddress.getText().toString())
                    .setPhone(etPhoneNumber.getText().toString())
                    .build();
        }

        return user;
    }

    public void setUser(User user) {
        this.user = user;

        clearFields();
        if (user == null) {
            return;
        }

        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etPhoneNumber.setText(user.getPhone());
        etEMailAddress.setText(user.getEmail());
    }

    public boolean isInputValid() {
        boolean hasError = false;
        if (TextUtils.isEmpty(etFirstName.getText().toString())) {
            hasError |= true;
            etFirstName.setError("Need first name");
        }
        if (TextUtils.isEmpty(etEMailAddress.getText().toString())) {
            hasError |= true;
            etEMailAddress.setError("Need e-mail address");
        }

        // check only for the minimal requirements
        return !hasError;
    }

}
