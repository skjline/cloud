package com.challenge.ct.mbs.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.object.Address;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.ui.component.AddressEntry;
import com.challenge.ct.mbs.ui.component.UserEntry;
import com.challenge.ct.mbs.utils.Const;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModifyFragment extends AbstractFragment {
    private static int[] menus = new int[]{R.id.action_save, R.id.action_clear};

    private View fragment;
    private UserEntry layoutUser;
    private AddressEntry layoutAddress;

    public ModifyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        fragment = inflater.inflate(R.layout.fragment_modify, container, false);

        layoutUser = (UserEntry) fragment.findViewById(R.id.layout_modify_contact_user);
        layoutAddress = (AddressEntry) fragment.findViewById(R.id.layout_modify_contact_address);

        Bundle argument = getArguments();
        if (argument != null && argument.containsKey(Const.TRANSITION_CONTACT_UID)) {
            String contactId = (String) argument.get(Const.TRANSITION_CONTACT_UID);

            User user = presenter.getUserById(contactId);

            if (user != null) {
                layoutUser.setUser(user);
                layoutAddress.setAddress(presenter.getAddressById(user.getAddressId()));
            }
        }

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_new_contact, menu);

        applyOptionsMenuStyle(menus, menu);
    }

    @Override
    public void updateUsers(List<User> users) {
        if (users == null) {
            Snackbar.make(fragment,
                    "We weren't able to update at this time.", Snackbar.LENGTH_SHORT).show();
        }

        triggerFragmentRequest(new FragmentEvent(Const.REQUEST_FRAGMENT_REMOVE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // layoutUser will validate inputs and create a new user
                User user = layoutUser.getUser();
                Address address = layoutAddress.getAddress();
                if (address == null || user == null) {
                    return true;
                }

                presenter.insertContact(user, address);
                return true;
            case R.id.action_clear:
                layoutUser.clearFields();
                layoutAddress.clearFields();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
