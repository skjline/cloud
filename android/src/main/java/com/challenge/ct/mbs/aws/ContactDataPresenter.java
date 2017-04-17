package com.challenge.ct.mbs.aws;

import android.text.TextUtils;
import android.util.Log;

import com.challenge.ct.mbs.object.Address;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.utils.Const;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Presenter binding contact view and objects via Backend DB Service
 */

public class ContactDataPresenter implements ContactContract.Presenter {
    private final ContactContract.View view;
    private final ContactDatabase database;

    private Disposable disposable;

    public ContactDataPresenter(ContactContract.View view, ContactDatabase database) {
        this.view = view;
        this.database = database;

        disposable = this.database.getOnDBStateChange()
                .subscribeOn(Schedulers.io())
                .filter(state ->
                        state.toLowerCase().equals(Const.CONNECTED) ||
                                state.toLowerCase().equals(Const.UPDATE))
                .subscribe(state -> refresh());
    }

    @Override
    public void refresh() {
        if (!database.isActive() || view == null) {
            return;
        }

        database.getUsers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::updateUsers);
    }

    @Override
    public void refresh(String partial) {
        if (!database.isActive() || view == null) {
            return;
        }

        database.getUsers(partial)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::updateUsers);
    }

    @Override
    public void insertContact(final User user, final Address address) {
        if (user == null || address == null) {
            return;
        }

        database.createOrUpdateAddress(address)
                .subscribe(addressId -> {
                    if (TextUtils.isEmpty(addressId)) {
                        Log.e("Update ContactAddress", "failed to update address");
                        if (view != null) {
                            view.updateUsers(null);
                        }
                        return;
                    }

                    user.setAddressId(addressId);
                    database.createOrUpdateUser(user)
                            .subscribe(contactId -> {
                                if (TextUtils.isEmpty(addressId)) {
                                    Log.e("Update ContactUser", "failed to update user");
                                    if (view != null) {
                                        view.updateUsers(null);
                                    }
                                }
                            });
                });
    }

    @Override
    public void updateUser(User user) {
        if (user == null) {
            return;
        }

        database.createOrUpdateUser(user)
                .subscribe(uid -> {
                    if (TextUtils.isEmpty(uid)) {
                        Log.e("Update ContactUser", "failed to update user");
                        if (view != null) {
                            view.updateUsers(null);
                        }
                    }
                });
    }

    @Override
    public void updateAddress(Address address) {
        if (address == null) {
            return;
        }

        database.createOrUpdateAddress(address)
                .subscribe(aid -> {
                    if (TextUtils.isEmpty(aid)) {
                        Log.e("Update ContactAddress", "Failed to update address");
                        if (view != null) {
                            view.updateUsers(null);
                        }
                    }
                });
    }

    @Override
    public void removeUser(User user) {
        if (user == null) {
            return;
        }

        database.removeUser(user)
                .subscribe(itemId -> {
                    if (TextUtils.isEmpty(itemId)) {
                        Log.e("Remove DynamoDB Item", "Failed to remove");
                    }
                });
    }

    @Override
    public User getUserById(String uid) {
        return database.getUserWithId(uid);
    }

    @Override
    public Address getAddressById(String aid) {
        return database.getAddressWithId(aid);
    }

    private void dispose() {
        disposable.dispose();
    }
}
