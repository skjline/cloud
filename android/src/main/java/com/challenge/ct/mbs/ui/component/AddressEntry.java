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
import com.challenge.ct.mbs.object.Address;

/**
 * File created on 3/6/17.
 */

public class AddressEntry extends LinearLayout {
    private Address address;

    private EditText etStreetMain, etStreetSub, etUnitNumber, etCity, etState,
            etZipCode, etCounty, etCountry;

    public AddressEntry(Context context) {
        super(context);
        initializeView(context);
    }

    public AddressEntry(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public AddressEntry(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    public AddressEntry(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context);
    }

    @Override
    public void setEnabled(boolean bool) {
        super.setEnabled(bool);

        etStreetMain.setEnabled(bool);
        etStreetSub.setEnabled(bool);
        etUnitNumber.setEnabled(bool);
        etCity.setEnabled(bool);
        etState.setEnabled(bool);
        etZipCode.setEnabled(bool);
        etCounty.setEnabled(bool);
        etCountry.setEnabled(bool);
    }

    private void initializeView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_address, this, true);

        etStreetMain = (EditText) view.findViewById(R.id.et_street_main);
        etStreetSub = (EditText) view.findViewById(R.id.et_street_sub);
        etUnitNumber = (EditText) view.findViewById(R.id.et_unit);
        etCity = (EditText) view.findViewById(R.id.et_city);
        etState = (EditText) view.findViewById(R.id.et_state);
        etZipCode = (EditText) view.findViewById(R.id.et_zip);
        etCounty = (EditText) view.findViewById(R.id.et_county);
        etCountry = (EditText) view.findViewById(R.id.et_country);
    }

    public void clearFields() {
        etStreetMain.setText("");
        etStreetSub.setText("");
        etUnitNumber.setText("");
        etCity.setText("");
        etState.setText("");
        etZipCode.setText("");
        etCounty.setText("");
        etCountry.setText("");
    }

    public Address getAddress() {
        if (!isInputValid()) {
            return null;
        }

        if (address == null) {
            if (TextUtils.isEmpty(etStreetMain.getText().toString())) {
                // user left all fields empty
                return null;
            }

            // create a new address from populated fields
            return new Address.Builder()
                    .setStreet1(etStreetMain.getText().toString())
                    .setStreet2(etStreetSub.getText().toString())
                    .setUnit(etUnitNumber.getText().toString())
                    .setCity(etCity.getText().toString())
                    .setState(etState.getText().toString())
                    .setZip(etZipCode.getText().toString())
                    .setCounty(etCounty.getText().toString())
                    .setCountry(etCountry.getText().toString())
                    .build();
        }

        return address;
    }

    public void setAddress(Address address) {
        this.address = address;

        clearFields();
        if (address == null) {
            return;
        }

        etStreetMain.setText(address.getStreet1());
        etStreetSub.setText(address.getStreet2());
        etUnitNumber.setText(address.getUnit());
        etCity.setText(address.getCity());
        etState.setText(address.getState());
        etZipCode.setText(address.getZip());
        etCounty.setText(address.getCounty());
        etCountry.setText(address.getCountry());
    }

    public boolean isInputValid() {
        int error = 0;
        if (TextUtils.isEmpty(etStreetMain.getText().toString())) {
            error++;
            etStreetMain.setError("Need street address");
        }
        if (TextUtils.isEmpty(etCity.getText().toString())) {
            error++;
            etCity.setError("Need city");
        }
        if (TextUtils.isEmpty(etState.getText().toString())) {
            error++;
            etState.setError("Need state");
        }
        if (TextUtils.isEmpty(etZipCode.getText().toString())) {
            error++;
            etZipCode.setError("Need zip");
        }

        if (error == 4) {
            // accepts when all value are empty
            return address == null &&
                    TextUtils.isEmpty(etStreetSub.getText().toString()) &&
                    TextUtils.isEmpty(etUnitNumber.getText().toString()) &&
                    TextUtils.isEmpty(etCounty.getText().toString()) &&
                    TextUtils.isEmpty(etCountry.getText().toString());
        }

        return 0 == error;
    }

}
