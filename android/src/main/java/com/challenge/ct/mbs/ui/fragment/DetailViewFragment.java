package com.challenge.ct.mbs.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.aws.ContactDatabase;
import com.challenge.ct.mbs.object.Address;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.utils.Const;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailViewFragment extends AbstractFragment {
    private static int[] menus = new int[]{R.id.action_edit, R.id.action_delete};

    AlertDialog alertRemoveItem;

    private User user;
    private String compoundAddress;

    public DetailViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_view, container, false);
        populateContents(getArguments(), view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_edit_contact, menu);

        applyOptionsMenuStyle(menus, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!ContactApplication.isSignedIn()) {
            listener.onEventTriggered(
                    new AbstractFragment.FragmentEvent(Const.GOOGLE_SIGN_IN_REQUEST_CODE));
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_edit:
                Bundle bundleEdit = new Bundle();
                bundleEdit.putInt(Const.TRANSITION_FRAGMENT_ID, R.layout.fragment_modify);
                bundleEdit.putString(Const.TRANSITION_CONTACT_UID, user.getUserId());

                triggerFragmentRequest(new FragmentEvent(Const.REQUEST_FRAGMENT_INSERT, bundleEdit));
                break;
            case R.id.action_delete:
                alertRemoveItem = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_remove_title)
                        .setMessage(R.string.alert_remove_message)
                        .setPositiveButton(R.string.alert_action_positive, (dialog, which) -> {
                            presenter.removeUser(user);
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.alert_action_negative, (dialog, which) -> dialog.dismiss())
                        .create();
                alertRemoveItem.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void updateUsers(List<User> users) {
        triggerFragmentRequest(new FragmentEvent(Const.REQUEST_FRAGMENT_REMOVE));
    }

    private void populateContents(Bundle bundle, View view) {
        if (bundle == null || !bundle.containsKey(Const.TRANSITION_CONTACT_UID)) {
            return;
        }

        ContactDatabase database = ContactApplication.getDatabase();

        User user = database.getUserWithId(bundle.getString(Const.TRANSITION_CONTACT_UID));
        if (user == null) {
            return;
        }

        this.user = user;
        ((TextView) view.findViewById(R.id.tv_name_value))
                .setText(this.user.getLastName().concat(TextUtils.isEmpty(this.user.getFirstName()) ? "" : ", " + this.user.getFirstName()));
        ((TextView) view.findViewById(R.id.tv_phone_value))
                .setText(this.user.getPhone());
        ((TextView) view.findViewById(R.id.tv_email_value))
                .setText(this.user.getEmail());

        View layout;
        layout = view.findViewById(R.id.layout_view_contact_phone);
        layout.setTag(1);
        layout.setOnClickListener(fabHandler);

        layout = view.findViewById(R.id.layout_view_contact_email);
        layout.setTag(2);
        layout.setOnClickListener(fabHandler);

        layout = view.findViewById(R.id.layout_view_contact_address);
        layout.setTag(3);
        layout.setOnClickListener(fabHandler);

        Address address;
        if (TextUtils.isEmpty(user.getAddressId()) ||
                (address = database.getAddressWithId(user.getAddressId())) == null) {
            return;
        }

        compoundAddress =
                address.getStreet1().concat(address.getStreet2()).concat(" ").concat(address.getUnit()).concat("\n")
                        .concat(address.getCity() + " ").concat(address.getState() + " ").concat(address.getZip()).concat("\n")
                        .concat(address.getCounty() + " ").concat(address.getCountry());

        ((TextView) view.findViewById(R.id.tv_address_value)).setText(compoundAddress);
    }

    private View.OnClickListener fabHandler = view -> {
        final int selection = (Integer) view.getTag();

        String dest;
        Intent intent;

        switch (selection) {
            case 1:
                dest = user.getPhone();
                intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", dest, null));
                break;
            case 2:
                dest = user.getEmail();
                intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", dest, null));
                break;
            case 3:
                dest = compoundAddress;
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + compoundAddress));
                intent.setPackage("com.google.android.apps.maps");
                break;
            default:
                Snackbar.make(view, R.string.snackbar_invalid_option, Snackbar.LENGTH_SHORT).show();
                return;
        }

        if (TextUtils.isEmpty(dest)) {
            Snackbar.make(view, R.string.snackbar_error, Snackbar.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
    };
}
