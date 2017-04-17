package com.challenge.ct.mbs.ui.fragment.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.aws.ContactContract;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.ui.fragment.AbstractFragment;
import com.challenge.ct.mbs.utils.Const;

import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link User}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactsRecyclerViewAdapter
        extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder>
        implements ContactContract.View {

    private List<User> users;

    private AbstractFragment.FragmentEventListener emitter;
    private View.OnClickListener onClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (emitter == null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt(Const.TRANSITION_FRAGMENT_ID, R.layout.fragment_detail);

            Object tag = v.getTag();
            if (tag != null && tag instanceof User) {
                bundle.putString(Const.TRANSITION_CONTACT_UID, String.valueOf(((User) tag).getUserId()));
            }

            emitter.onEventTriggered(new AbstractFragment.FragmentEvent(
                    Const.REQUEST_FRAGMENT_INSERT, bundle
            ));
        }
    };

    public ContactsRecyclerViewAdapter(AbstractFragment.FragmentEventListener emitter) {
        this.emitter = emitter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (users == null) {
            // not initialized
            return;
        }

        User user = users.get(position);
        holder.mItem = users.get(position);

        holder.mIdView.setText(String.format(Locale.getDefault(),
                "%s, %s", user.getLastName(), user.getFirstName()));

        // try getting phone number then e-mail if neither exist, display "unknown"
        String info = user.getPhone() != null ? user.getPhone() :
                user.getEmail() != null ? user.getEmail() : "Unknown";
        holder.mContentView.setText(info);

        holder.mView.setOnClickListener(onClicked);
        holder.mView.setTag(user);
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    @Override
    public void updateUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public User mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
