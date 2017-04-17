package com.challenge.ct.mbs.aws;

import com.challenge.ct.mbs.object.Address;
import com.challenge.ct.mbs.object.User;

import java.util.List;

/**
 * User contract
 */

public interface ContactContract {
    interface View {
        void updateUsers(List<User> users);
    }

    interface Presenter {
        void refresh();
        void refresh(String partial);

        void insertContact(User user, Address address);
        void updateUser(User user);
        void updateAddress(Address address);
        void removeUser(User user);

        User getUserById(String uid);
        Address getAddressById(String aid);
    }
}
